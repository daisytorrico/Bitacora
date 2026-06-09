package com.catedra.bitacora.features.discovery.data.repository

import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.features.discovery.data.remote.DiscoveryRemoteDataSource
import com.catedra.bitacora.features.discovery.domain.model.TravelPage
import com.catedra.bitacora.features.discovery.domain.repository.DiscoveryRepository
import com.catedra.bitacora.features.discovery.presentation.explorer.DurationFilter
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

@Singleton
class DiscoveryRepositoryImpl @Inject constructor(
    private val remoteDataSource: DiscoveryRemoteDataSource
) : DiscoveryRepository {

    override suspend fun getPublicTravels(
        limit: Long, 
        lastDocument: Any?,
        excludeOwnerIds: List<String>
    ): Result<TravelPage> = runCatching {
        val startAfter = lastDocument as? DocumentSnapshot
        
        val result = remoteDataSource.getPublicTravels(limit, startAfter, excludeOwnerIds)
        
        TravelPage(
            travels = result.travels,
            lastDocument = result.lastDocument
        )
    }

    override suspend fun getFollowingTravels(
        limit: Long,
        lastDocument: Any?
    ): Result<TravelPage> = runCatching {
        val startAfter = lastDocument as? DocumentSnapshot
        val result = remoteDataSource.getFollowingTravels(limit, startAfter)

        TravelPage(
            travels = result.travels,
            lastDocument = result.lastDocument
        )
    }
    
    override suspend fun getFollowingIds(): Result<List<String>> = runCatching {
        remoteDataSource.getFollowingIds()
    }

    override suspend fun getPointsCount(travelId: String): Result<Int> = runCatching {
        remoteDataSource.getPointsCount(travelId).toInt()
    }

    override suspend fun getPublicProfile(userId: String): Result<User> = runCatching {
        remoteDataSource.getPublicProfile(userId)
    }

    override suspend fun getPublicUserTravels(userId: String): Result<List<Travel>> = runCatching {
        remoteDataSource.getPublicUserTravels(userId)
    }

    override suspend fun getPublicTravelDetail(travelId: String): Result<Travel> = runCatching {
        remoteDataSource.getPublicTravelDetail(travelId)
    }

    override suspend fun getPublicPointsOfInterest(travelId: String): Result<List<PointOfInterest>> = runCatching {
        remoteDataSource.getPublicPointsOfInterest(travelId)
    }

    override suspend fun getPublicPointDetail(travelId: String, pointId: String): Result<PointOfInterest> = runCatching {
        remoteDataSource.getPublicPointDetail(travelId, pointId)
    }

    override suspend fun followUser(userId: String): Result<Unit> = runCatching {
        remoteDataSource.followUser(userId)
    }

    override suspend fun unfollowUser(userId: String): Result<Unit> = runCatching {
        remoteDataSource.unfollowUser(userId)
    }

    override suspend fun isFollowing(userId: String): Result<Boolean> = runCatching {
        remoteDataSource.isFollowing(userId)
    }
    override suspend fun searchTravels(query: String): Result<List<Travel>> = runCatching {
        remoteDataSource.searchTravels(query)
    }
    override suspend fun getFilteredTravels(
        limit: Long,
        lastDocument: Any?,
        searchQuery: String?,
        durationFilter: DurationFilter?,
        isDetailedOnly: Boolean,
        selectedMonth: Int?,
        selectedYear: Int?
    ): Result<TravelPage> = runCatching {
        val startAfter = lastDocument as? DocumentSnapshot
        val result = remoteDataSource.getFilteredTravels(
            limit = limit,
            lastDocument = startAfter,
            searchQuery = searchQuery,
            durationFilter = durationFilter,
            isDetailedOnly = isDetailedOnly,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear
        )
        TravelPage(travels = result.travels, lastDocument = result.lastDocument)
    }
}
