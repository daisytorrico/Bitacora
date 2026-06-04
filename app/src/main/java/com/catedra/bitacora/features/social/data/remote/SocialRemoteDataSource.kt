package com.catedra.bitacora.features.social.data.remote

import android.util.Log
import com.catedra.bitacora.features.social.domain.model.Comment
import com.catedra.bitacora.features.social.data.model.CommentData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    // --- GENERIC LIKES ---

    suspend fun toggleLike(collectionPath: String, isLiked: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val docRef = firestore.collection(collectionPath).document(userId)
        val parentRef = firestore.document(collectionPath.removeSuffix("/likes"))

        if (isLiked) {
            docRef.delete().await()
            runCatching { parentRef.update("likesCount", FieldValue.increment(-1)).await() }
        } else {
            docRef.set(mapOf("timestamp" to FieldValue.serverTimestamp())).await()
            runCatching { parentRef.update("likesCount", FieldValue.increment(1)).await() }
        }
    }

    // Escucha en tiempo real si el usuario actual likeó el documento en [collectionPath]/likes
    fun isLiked(collectionPath: String): Flow<Boolean> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        val subscription = firestore.collection(collectionPath).document(userId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.exists() == true)
            }
        awaitClose { subscription.remove() }
    }

    fun getLikesCount(tripId: String, poiId: String): Flow<Int> = callbackFlow {
        val subscription = firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("likes")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) trySend(snapshot.size())
            }
        awaitClose { subscription.remove() }
    }

    // --- COMENTARIOS ---

    fun getCommentsCount(tripId: String, poiId: String): Flow<Int> = callbackFlow {
        val subscription = firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("comments")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) trySend(snapshot.size())
            }
        awaitClose { subscription.remove() }
    }

    fun getComments(tripId: String, poiId: String): Flow<List<CommentData>> = callbackFlow {
        val subscription = firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val comments = snapshot.toObjects(CommentData::class.java)
                    launch {
                        val userIds = comments.map { it.userId }.distinct()
                        val userMap = userIds.associateWith { uid ->
                            val doc = firestore.collection("users").document(uid).get().await()
                            val name = doc.getString("username") ?: doc.getString("nombre") ?: "Anónimo"
                            Pair(name, doc.getString("photoUrl"))
                        }
                        trySend(comments.map { c ->
                            val userData = userMap[c.userId]
                            c.copy(username = userData?.first ?: "Anónimo", userPhotoUrl = userData?.second)
                        })
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveComment(tripId: String, poiId: String, content: String, parentId: String? = null) {
        val user = auth.currentUser ?: return
        val data = hashMapOf(
            "userId" to user.uid,
            "content" to content,
            "timestamp" to FieldValue.serverTimestamp(),
            "likesCount" to 0
        )

        if (parentId == null) {
            firestore.collection("trips").document(tripId)
                .collection("pointsOfInterest").document(poiId)
                .collection("comments").add(data).await()
        } else {
            firestore.collection("trips").document(tripId)
                .collection("pointsOfInterest").document(poiId)
                .collection("comments").document(parentId)
                .collection("replies").add(data).await()
        }
    }

    // --- RESPUESTAS (REPLIES) ---

    // Inyecta commentId como parentId en cada reply para que el ViewModel
    // pueda construir el path de likes sin leer Firestore extra
    fun getReplies(tripId: String, poiId: String, commentId: String): Flow<List<CommentData>> = callbackFlow {
        val subscription = firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("comments").document(commentId)
            .collection("replies")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val replies = snapshot.toObjects(CommentData::class.java)
                    launch {
                        val userIds = replies.map { it.userId }.distinct()
                        val userMap = userIds.associateWith { uid ->
                            val doc = firestore.collection("users").document(uid).get().await()
                            val name = doc.getString("username") ?: doc.getString("nombre") ?: "Anónimo"
                            Pair(name, doc.getString("photoUrl"))
                        }
                        trySend(replies.map { r ->
                            val userData = userMap[r.userId]
                            r.copy(
                                username = userData?.first ?: "Anónimo",
                                userPhotoUrl = userData?.second,
                                parentId = commentId
                            )
                        })
                    }
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun deleteSocialMessage(tripId: String, poiId: String, commentId: String, replyId: String? = null) {
        if (replyId == null) {
            val commentRef = firestore.collection("trips").document(tripId)
                .collection("pointsOfInterest").document(poiId)
                .collection("comments").document(commentId)

            val refsToDelete = mutableListOf<DocumentReference>()
            refsToDelete.add(commentRef)

            val commentLikes = commentRef.collection("likes").get().await()
            commentLikes.documents.forEach { refsToDelete.add(it.reference) }

            val repliesSnapshot = commentRef.collection("replies").get().await()
            for (replyDoc in repliesSnapshot.documents) {
                refsToDelete.add(replyDoc.reference)
                val replyLikes = replyDoc.reference.collection("likes").get().await()
                replyLikes.documents.forEach { refsToDelete.add(it.reference) }
            }

            refsToDelete.chunked(500).forEach { chunk ->
                firestore.runBatch { batch -> chunk.forEach { batch.delete(it) } }.await()
            }
        } else {
            val replyRef = firestore.collection("trips").document(tripId)
                .collection("pointsOfInterest").document(poiId)
                .collection("comments").document(commentId)
                .collection("replies").document(replyId)

            val likesSnapshot = replyRef.collection("likes").get().await()

            firestore.runBatch { batch ->
                likesSnapshot.documents.forEach { batch.delete(it.reference) }
                batch.delete(replyRef)
            }.await()
        }
    }

    // --- SOCIAL / SEGUIDORES ---

    suspend fun followUser(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.runTransaction { transaction ->
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val targetUserRef = firestore.collection("users").document(targetUserId)
            val followRef = firestore.collection("followers").document(currentUserId)
                .collection("following").document(targetUserId)

            transaction.set(followRef, mapOf("createdAt" to FieldValue.serverTimestamp()))
            transaction.update(currentUserRef, "followingCount", FieldValue.increment(1))
            transaction.update(targetUserRef, "followersCount", FieldValue.increment(1))
        }.await()
    }

    suspend fun unfollowUser(targetUserId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        firestore.runTransaction { transaction ->
            val currentUserRef = firestore.collection("users").document(currentUserId)
            val targetUserRef = firestore.collection("users").document(targetUserId)
            val followRef = firestore.collection("followers").document(currentUserId)
                .collection("following").document(targetUserId)

            transaction.delete(followRef)
            transaction.update(currentUserRef, "followingCount", FieldValue.increment(-1))
            transaction.update(targetUserRef, "followersCount", FieldValue.increment(-1))
        }.await()
    }

    suspend fun isFollowing(targetUserId: String): Boolean {
        val currentUserId = auth.currentUser?.uid ?: return false
        val doc = firestore.collection("followers").document(currentUserId)
            .collection("following").document(targetUserId).get().await()
        return doc.exists()
    }

    suspend fun getFollowingIds(): List<String> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = firestore.collection("followers").document(userId)
            .collection("following").get().await()
        return snapshot.documents.map { it.id }
    }
}