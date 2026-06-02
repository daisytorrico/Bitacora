package com.catedra.bitacora.features.discovery.domain.repository

import com.catedra.bitacora.features.auth.domain.model.User
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.discovery.domain.model.TravelPage
import com.catedra.bitacora.features.travel.domain.model.Travel

interface DiscoveryRepository {
    suspend fun getPublicTravels(
        limit: Long = 10, 
        lastDocument: Any? = null,
        excludeOwnerIds: List<String> = emptyList()
    ): Result<TravelPage>
    
    suspend fun getFollowingTravels(
        limit: Long = 10,
        lastDocument: Any? = null
    ): Result<TravelPage>
    
    suspend fun getFollowingIds(): Result<List<String>>
    
    suspend fun getPointsCount(travelId: String): Result<Int>

    suspend fun getPublicProfile(userId: String): Result<User>
    suspend fun getPublicUserTravels(userId: String): Result<List<Travel>>
    suspend fun getPublicTravelDetail(travelId: String): Result<Travel>
    suspend fun getPublicPointsOfInterest(travelId: String): Result<List<PointOfInterest>>
    suspend fun getPublicPointDetail(travelId: String, pointId: String): Result<PointOfInterest>
    suspend fun followUser(userId: String): Result<Unit>
    suspend fun unfollowUser(userId: String): Result<Unit>
    suspend fun isFollowing(userId: String): Result<Boolean>
}
