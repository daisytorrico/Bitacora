package com.catedra.bitacora.features.travel.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class TravelRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun getTravels(userId: String): QuerySnapshot {
        return db.collection("trips")
            .whereEqualTo("ownerId", userId)
            .get()
            .await()
    }

    suspend fun saveTravel(travelData: Map<String, Any?>) {
        db.collection("trips")
            .add(travelData)
            .await()
    }
}
