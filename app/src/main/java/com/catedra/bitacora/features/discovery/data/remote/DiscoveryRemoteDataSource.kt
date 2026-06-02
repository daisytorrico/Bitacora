package com.catedra.bitacora.features.discovery.data.remote

import com.catedra.bitacora.features.auth.data.mapper.toUser
import com.catedra.bitacora.features.auth.domain.model.User
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
        val allExcluded = excludeOwnerIds + currentUserId
        
        val fetchLimit = limit + 20
        
        var query = firestore.collection("trips")
            .whereEqualTo("visibility", TravelVisibility.PUBLIC.name.lowercase())
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .limit(fetchLimit)
        
        if (lastDocument != null) {
            query = query.startAfter(lastDocument)
        }

        val snapshot = query.get().await()
        val travels = mutableListOf<Travel>()
        var lastProcessedDoc: DocumentSnapshot? = null

        for (doc in snapshot.documents) {
            val travel = doc.toTravel()
            // Quitamos el filtro de travel.ownerId !in allExcluded para que el usuario 
            // pueda ver sus propios viajes públicos en la pestaña de exploración.
            if (travel != null) {
                travels.add(travel)
                lastProcessedDoc = doc
                if (travels.size >= limit) break
            }
            lastProcessedDoc = doc
        }
        
        return TravelPageRemote(travels, lastProcessedDoc)
    }

    suspend fun getFollowingTravels(
        limit: Long,
        lastDocument: DocumentSnapshot? = null
    ): TravelPageRemote {
        val followingIds = getFollowingIds()
        if (followingIds.isEmpty()) return TravelPageRemote(emptyList(), null)

        var query = firestore.collection("trips")
            .whereIn("ownerId", followingIds)
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
                baseQuery.whereIn("visibility", listOf("public", "followers")).get().await()
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
}
