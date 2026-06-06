package com.catedra.bitacora.features.discovery.data.remote

import android.util.Log
import com.catedra.bitacora.features.auth.data.mapper.toUser
import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.features.discovery.data.remote.model.TravelPageRemote
import com.catedra.bitacora.features.travel.data.mapper.toPointOfInterest
import com.catedra.bitacora.features.travel.data.mapper.toPointsDomain
import com.catedra.bitacora.features.travel.data.mapper.toTravel
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.model.TravelVisibility
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoveryRemoteDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    suspend fun getPublicTravels(
        limit: Long,
        lastDocument: DocumentSnapshot? = null,
        excludeOwnerIds: List<String> = emptyList()
    ): TravelPageRemote {
        val currentUserId = auth.currentUser?.uid ?: ""
        val followingIds = getFollowingIds()
        
        // Lista negra: Mis viajes + los que ya sigo
        val blackList = (excludeOwnerIds + currentUserId + followingIds).toSet()
        
        val resultTravels = mutableListOf<Travel>()
        var lastDoc = lastDocument
        
        var attempts = 0
        while (resultTravels.size < limit && attempts < 3) {
            var query = firestore.collection("trips")
                .whereEqualTo("visibility", TravelVisibility.PUBLIC.name.lowercase())
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .limit(limit * 2) 

            if (lastDoc != null) {
                query = query.startAfter(lastDoc)
            }

            val snapshot = query.get().await()
            if (snapshot.isEmpty) break

            for (doc in snapshot.documents) {
                val travel = doc.toTravel()
                lastDoc = doc
                
                if (travel != null && travel.ownerId !in blackList) {
                    resultTravels.add(travel)
                    if (resultTravels.size >= limit) break
                }
            }
            attempts++
        }
        
        return TravelPageRemote(resultTravels, lastDoc)
    }

    suspend fun getFollowingTravels(
        limit: Long,
        lastDocument: DocumentSnapshot? = null
    ): TravelPageRemote {
        val currentUserId = auth.currentUser?.uid ?: ""
        val followingIds = getFollowingIds().filter { it != currentUserId }
        
        if (followingIds.isEmpty()) return TravelPageRemote(emptyList(), null)

        val queryIds = followingIds.take(30)

        var query = firestore.collection("trips")
            .whereIn("ownerId", queryIds)
            .whereIn("visibility", listOf("public", "followers"))
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(limit)

        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }

        val snapshot = query.get().await()
        val travels = snapshot.documents.mapNotNull { it.toTravel() }
            
        return TravelPageRemote(travels, snapshot.documents.lastOrNull())
    }

    suspend fun getPointsCount(travelId: String): Long {
        return firestore.collection("trips")
            .document(travelId)
            .collection("pointsOfInterest")
            .count()
            .get(com.google.firebase.firestore.AggregateSource.SERVER)
            .await()
            .count
    }

    suspend fun getPublicProfile(userId: String): User {
        val doc = firestore.collection("users").document(userId).get().await()
        return doc.toUser()
    }

    suspend fun getPublicUserTravels(userId: String): List<Travel> {
        val currentUserId = auth.currentUser?.uid
        val isMe = currentUserId == userId

        val baseQuery = firestore.collection("trips").whereEqualTo("ownerId", userId)

        val snapshot = if (isMe) {
            baseQuery.get().await()
        } else {
            try {
                baseQuery
                    .whereIn("visibility", listOf("public", "followers"))
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get().await()
            } catch (e: Exception) {
                baseQuery.whereEqualTo("visibility", "public").get().await()
            }
        }

        return snapshot.documents.mapNotNull { it.toTravel() }
    }

    suspend fun getPublicTravelDetail(travelId: String): Travel {
        val doc = firestore.collection("trips").document(travelId).get().await()
        return doc.toTravel() ?: throw Exception("Viaje no encontrado")
    }

    suspend fun getPublicPointsOfInterest(travelId: String): List<PointOfInterest> {
        return firestore.collection("trips").document(travelId)
            .collection("pointsOfInterest")
            .get()
            .await()
            .toPointsDomain()
    }

    suspend fun getPublicPointDetail(travelId: String, pointId: String): PointOfInterest {
        return firestore.collection("trips").document(travelId)
            .collection("pointsOfInterest").document(pointId)
            .get()
            .await()
            .toPointOfInterest()
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
    
    suspend fun getFollowingIds(): List<String> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val snapshot = firestore.collection("followers").document(userId)
            .collection("following").get().await()
        return snapshot.documents.map { it.id }
    }
    suspend fun searchTravels(query: String): List<Travel> {
        val currentUserId = auth.currentUser?.uid ?: ""
        val followingIds = getFollowingIds()
        val end = query + '\uf8ff'

        val publicDeferred = firestore.collection("trips")
            .whereEqualTo("visibility", TravelVisibility.PUBLIC.name.lowercase())
            .whereGreaterThanOrEqualTo("name", query)
            .whereLessThanOrEqualTo("name", end)
            .limit(20)
            .get().await()

        val publicTravels = publicDeferred.documents
            .mapNotNull { it.toTravel() }
            .filter { it.ownerId != currentUserId }

        val followingTravels = if (followingIds.isNotEmpty()) {
            firestore.collection("trips")
                .whereIn("ownerId", followingIds.take(30))
                .whereIn("visibility", listOf("public", "followers"))
                .limit(50)
                .get().await()
                .documents.mapNotNull { it.toTravel() }
                .filter { it.name.startsWith(query, ignoreCase = true) }
        } else emptyList()

        return (publicTravels + followingTravels).distinctBy { it.id }
    }
}
