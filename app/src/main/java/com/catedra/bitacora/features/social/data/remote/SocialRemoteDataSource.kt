package com.catedra.bitacora.features.social.data.remote

import com.catedra.bitacora.features.social.domain.model.Comment
import com.catedra.bitacora.features.social.domain.model.Reply
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun giveLike(tripId: String, poiId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("likes").document(userId)
            .set(mapOf("timestamp" to FieldValue.serverTimestamp()))
            .await()
    }

    suspend fun removeLike(tripId: String, poiId: String) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("likes").document(userId)
            .delete()
            .await()
    }

    fun getLikesCount(tripId: String, poiId: String): Flow<Int> = callbackFlow {
        val subscription = firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("likes")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.size())
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getCommentsCount(tripId: String, poiId: String): Flow<Int> = callbackFlow {
        val subscription = firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("comments")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.size())
                }
            }
        awaitClose { subscription.remove() }
    }

    fun isLiked(tripId: String, poiId: String): Flow<Boolean> = callbackFlow {
        val userId = auth.currentUser?.uid ?: return@callbackFlow
        val subscription = firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("likes").document(userId)
            .addSnapshotListener { snapshot, _ ->
                trySend(snapshot?.exists() == true)
            }
        awaitClose { subscription.remove() }
    }

    fun getComments(tripId: String, poiId: String): Flow<List<Comment>> = callbackFlow {
        val subscription = firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val comments = snapshot.documents.map { doc ->
                        Comment(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            username = doc.getString("username") ?: "",
                            userPhotoUrl = doc.getString("userPhotoUrl"),
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getTimestamp("timestamp")?.toDate()?.toInstant()
                                ?.atZone(ZoneId.systemDefault())?.toLocalDateTime() ?: LocalDateTime.now()
                        )
                    }
                    trySend(comments)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveComment(tripId: String, poiId: String, content: String) {
        val user = auth.currentUser ?: return
        val comment = hashMapOf(
            "userId" to user.uid,
            "username" to (user.displayName ?: "Anónimo"),
            "userPhotoUrl" to user.photoUrl?.toString(),
            "content" to content,
            "timestamp" to FieldValue.serverTimestamp()
        )
        firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("comments").add(comment).await()
    }

    fun getReplies(tripId: String, poiId: String, commentId: String): Flow<List<Reply>> = callbackFlow {
        val subscription = firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("comments").document(commentId)
            .collection("replies")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val replies = snapshot.documents.map { doc ->
                        Reply(
                            id = doc.id,
                            userId = doc.getString("userId") ?: "",
                            username = doc.getString("username") ?: "",
                            userPhotoUrl = doc.getString("userPhotoUrl"),
                            content = doc.getString("content") ?: "",
                            timestamp = doc.getTimestamp("timestamp")?.toDate()?.toInstant()
                                ?.atZone(ZoneId.systemDefault())?.toLocalDateTime() ?: LocalDateTime.now()
                        )
                    }
                    trySend(replies)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveReply(tripId: String, poiId: String, commentId: String, content: String) {
        val user = auth.currentUser ?: return
        val reply = hashMapOf(
            "userId" to user.uid,
            "username" to (user.displayName ?: "Anónimo"),
            "userPhotoUrl" to user.photoUrl?.toString(),
            "content" to content,
            "timestamp" to FieldValue.serverTimestamp()
        )
        firestore.collection("trips").document(tripId)
            .collection("pointsOfInterest").document(poiId)
            .collection("comments").document(commentId)
            .collection("replies").add(reply).await()
    }

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
}
