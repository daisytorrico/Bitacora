package com.catedra.bitacora.features.travel.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class TravelRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun getTravels(userId: String): List<DocumentSnapshot> {
        val results = mutableListOf<DocumentSnapshot>()

        try {
            val ownedQuery = db.collection("trips")
                .whereEqualTo("ownerId", userId)
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .await()
            results.addAll(ownedQuery.documents)
        } catch (e: Exception) {
            Log.e("TravelDataSource", "Error owned query: ${e.message}", e)
        }

        return results.distinctBy { it.id }
    }

    suspend fun saveTravel(travelData: Map<String, Any?>): String {
        val dataWithTimestamp = travelData.toMutableMap().apply {
            put("updatedAt", FieldValue.serverTimestamp())
            if (!containsKey("privileges")) put("privileges", emptyList<String>())
        }
        val documentReference = db.collection("trips")
            .add(dataWithTimestamp)
            .await()
        return documentReference.id
    }

    suspend fun getTravelById(travelId: String): DocumentSnapshot {
        return db.collection("trips").document(travelId).get().await()
    }

    suspend fun getPointsOfInterest(travelId: String): QuerySnapshot {
        return db.collection("trips")
            .document(travelId)
            .collection("pointsOfInterest")
            .get()
            .await()
    }

    suspend fun getPointOfInterest(travelId: String, pointId: String): DocumentSnapshot {
        return db.collection("trips")
            .document(travelId)
            .collection("pointsOfInterest")
            .document(pointId)
            .get()
            .await()
    }

    suspend fun getPointsCount(travelId: String): Long {
        return db.collection("trips")
            .document(travelId)
            .collection("pointsOfInterest")
            .count()
            .get(com.google.firebase.firestore.AggregateSource.SERVER)
            .await()
            .count
    }

    suspend fun savePoint(travelId: String, pointData: Map<String, Any?>): String {
        val batch = db.batch()
        val pointRef = db.collection("trips")
            .document(travelId)
            .collection("pointsOfInterest")
            .document()
        batch.set(pointRef, pointData)
        val tripRef = db.collection("trips").document(travelId)
        batch.update(tripRef, "pointsCount", FieldValue.increment(1))
        batch.update(tripRef, "updatedAt", FieldValue.serverTimestamp())
        batch.commit().await()
        return pointRef.id
    }

    suspend fun updateTravel(travelId: String, travelData: Map<String, Any?>) {
        val dataWithTimestamp = travelData.toMutableMap().apply {
            put("updatedAt", FieldValue.serverTimestamp())
        }
        db.collection("trips").document(travelId).update(dataWithTimestamp).await()
    }

    suspend fun updatePoint(travelId: String, pointId: String, pointData: Map<String, Any?>) {
        val batch = db.batch()
        val pointRef = db.collection("trips")
            .document(travelId)
            .collection("pointsOfInterest")
            .document(pointId)
        batch.update(pointRef, pointData)
        val tripRef = db.collection("trips").document(travelId)
        batch.update(tripRef, "updatedAt", FieldValue.serverTimestamp())
        batch.commit().await()
    }

    suspend fun syncTripAccess(travelId: String, newPrivileges: List<String>, removed: List<String>) {
        val batch = db.batch()

        removed.forEach { uid ->
            val ref = db.collection("tripAccess")
                .document(uid)
                .collection("trips")
                .document(travelId)
            batch.delete(ref)
        }

        newPrivileges.forEach { uid ->
            val ref = db.collection("tripAccess")
                .document(uid)
                .collection("trips")
                .document(travelId)
            batch.set(ref, mapOf("addedAt" to FieldValue.serverTimestamp()))
        }

        batch.commit().await()
    }

    suspend fun getSharedTripIds(userId: String): List<String> {
        val snapshot = db.collection("tripAccess")
            .document(userId)
            .collection("trips")
            .get()
            .await()
        return snapshot.documents.map { it.id }
    }

    suspend fun deletePoint(travelId: String, pointId: String) {
        val pointRef = db.collection("trips").document(travelId)
            .collection("pointsOfInterest").document(pointId)

        coroutineScope {
            // Traer likes del punto y comentarios en paralelo
            val likesDeferred = async { pointRef.collection("likes").get().await() }
            val commentsDeferred = async { pointRef.collection("comments").get().await() }
            
            val likes = likesDeferred.await()
            val comments = commentsDeferred.await()

            // Para cada comentario, traer sus likes y sus replies en paralelo
            val commentDataDeferred = comments.documents.map { comment ->
                async {
                    val cLikes = comment.reference.collection("likes").get().await()
                    val cReplies = comment.reference.collection("replies").get().await()
                    Triple(comment, cLikes, cReplies)
                }
            }
            val commentData = commentDataDeferred.map { it.await() }

            // Para cada reply, traer sus likes en paralelo
            val replyLikesDeferred = commentData.flatMap { triple ->
                triple.third.documents.map { reply ->
                    async { reply.reference.collection("likes").get().await() to reply.reference }
                }
            }
            val replyLikes = replyLikesDeferred.map { it.await() }

            // Armar la lista de todas las referencias a borrar
            val toDelete = mutableListOf<com.google.firebase.firestore.DocumentReference>()
            
            // 1. Likes del punto
            likes.documents.forEach { toDelete.add(it.reference) }
            
            // 2. Comentarios, sus likes y sus replies
            commentData.forEach { (comment, cLikes, cReplies) ->
                cLikes.documents.forEach { toDelete.add(it.reference) }
                toDelete.add(comment.reference)
            }

            // 3. Replies y sus likes
            replyLikes.forEach { (rLikes, replyRef) ->
                rLikes.documents.forEach { toDelete.add(it.reference) }
                toDelete.add(replyRef)
            }

            // 4. El punto mismo
            toDelete.add(pointRef)

            // Borrar en batches de 498 para dejar lugar al decremento y updatedAt
            val tripRef = db.collection("trips").document(travelId)
            toDelete.chunked(498).forEachIndexed { index, chunk ->
                val batch = db.batch()
                chunk.forEach { batch.delete(it) }
                if (index == toDelete.chunked(498).lastIndex) {
                    batch.update(tripRef, "pointsCount", FieldValue.increment(-1))
                    batch.update(tripRef, "updatedAt", FieldValue.serverTimestamp())
                }
                batch.commit().await()
            }
        }
    }

    suspend fun deleteTravel(travelId: String) {
        val tripRef = db.collection("trips").document(travelId)

        coroutineScope {
            // 1. Obtener datos del viaje para saber quiénes tienen acceso (para limpiar tripAccess)
            val tripDoc = tripRef.get().await()
            val ownerId = tripDoc.getString("ownerId")
            val privileges = tripDoc.get("privileges") as? List<String> ?: emptyList()
            val allUsersWithAccess = (privileges + (ownerId ?: "")).filter { it.isNotEmpty() }.distinct()

            // 2. Obtener todos los puntos de interés
            val points = tripRef.collection("pointsOfInterest").get().await()

            val toDelete = mutableListOf<com.google.firebase.firestore.DocumentReference>()

            // 3. Por cada punto, recolectar sub-recursos (similar a deletePoint)
            // Procesamos los puntos en paralelo para mayor velocidad
            val pointsDataDeferred = points.documents.map { point ->
                async {
                    val pointRef = point.reference
                    val pointSubToDelete = mutableListOf<com.google.firebase.firestore.DocumentReference>()

                    val likes = pointRef.collection("likes").get().await()
                    val comments = pointRef.collection("comments").get().await()

                    likes.documents.forEach { pointSubToDelete.add(it.reference) }

                    for (comment in comments.documents) {
                        val cLikes = comment.reference.collection("likes").get().await()
                        val cReplies = comment.reference.collection("replies").get().await()

                        cLikes.documents.forEach { pointSubToDelete.add(it.reference) }

                        for (reply in cReplies.documents) {
                            val rLikes = reply.reference.collection("likes").get().await()
                            rLikes.documents.forEach { pointSubToDelete.add(it.reference) }
                            pointSubToDelete.add(reply.reference)
                        }
                        pointSubToDelete.add(comment.reference)
                    }
                    pointSubToDelete.add(pointRef)
                    pointSubToDelete
                }
            }

            // Esperamos a que todos los puntos recolecten sus referencias
            pointsDataDeferred.forEach { toDelete.addAll(it.await()) }

            // 4. Recolectar referencias de tripAccess para todos los usuarios involucrados
            allUsersWithAccess.forEach { uid ->
                toDelete.add(
                    db.collection("tripAccess")
                        .document(uid)
                        .collection("trips")
                        .document(travelId)
                )
            }

            // 5. El viaje mismo
            toDelete.add(tripRef)

            // 6. Borrar en batches de 500 (límite de Firestore)
            toDelete.chunked(500).forEach { chunk ->
                val batch = db.batch()
                chunk.forEach { batch.delete(it) }
                batch.commit().await()
            }
        }
    }

}