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
            //Traer likes y comments en paralelo
            val likesDeferred = async { pointRef.collection("likes").get().await() }
            val commentsDeferred = async { pointRef.collection("comments").get().await() }
            val likes = likesDeferred.await()
            val comments = commentsDeferred.await()

            //Traer todas las replies en paralelo
            val repliesByComment = comments.documents.map { comment ->
                async { comment.reference.collection("replies").get().await() }
            }.map { it.await() }

            //Armar la lista de todas las referencias a borrar
            val toDelete = mutableListOf<com.google.firebase.firestore.DocumentReference>()
            likes.documents.forEach { toDelete.add(it.reference) }
            comments.documents.forEachIndexed { index, comment ->
                repliesByComment[index].documents.forEach { toDelete.add(it.reference) }
                toDelete.add(comment.reference)
            }
            toDelete.add(pointRef)

            //Borrar en batches de 498 para dejar lugar al decremento y updatedAt
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

}