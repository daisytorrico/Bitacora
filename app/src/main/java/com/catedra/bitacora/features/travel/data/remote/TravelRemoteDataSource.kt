package com.catedra.bitacora.features.travel.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class TravelRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun getTravels(userId: String): List<DocumentSnapshot> {
        val results = mutableListOf<DocumentSnapshot>()
        
        // Consulta 1: Viajes donde soy dueño
        try {
            val ownedQuery = db.collection("trips")
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
            results.addAll(ownedQuery.documents)
        } catch (e: Exception) {
        }

        // Consulta 2: Viajes donde soy colaborador
        try {
            val sharedQuery = db.collection("trips")
                .whereArrayContains("privileges", userId)
                .get()
                .await()
            results.addAll(sharedQuery.documents)
        } catch (e: Exception) {
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
        batch.update(tripRef, "updatedAt", FieldValue.serverTimestamp())
        
        batch.commit().await()
        return pointRef.id
    }
}
