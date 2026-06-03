package com.catedra.bitacora.core.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class NearbyPointsRemoteDataSource @Inject constructor(
    private val db: FirebaseFirestore
) {
    suspend fun getAuthorizedNearbyPoints(
        userId: String,
        range: Pair<String, String>
    ): QuerySnapshot {
        return db.collectionGroup("pointsOfInterest")
            .whereArrayContains("authorizedUsers", userId)
            .whereGreaterThanOrEqualTo("geohash", range.first)
            .whereLessThanOrEqualTo("geohash", range.second)
            .get()
            .await()
    }

    suspend fun getPointsByTripAndGeohash(
        tripId: String,
        range: Pair<String, String>
    ): QuerySnapshot {
        return db.collection("trips")
            .document(tripId)
            .collection("pointsOfInterest")
            .whereGreaterThanOrEqualTo("geohash", range.first)
            .whereLessThanOrEqualTo("geohash", range.second)
            .get()
            .await()
    }
}
