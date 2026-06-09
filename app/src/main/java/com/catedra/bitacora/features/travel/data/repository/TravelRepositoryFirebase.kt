package com.catedra.bitacora.features.travel.data.repository

import com.catedra.bitacora.features.travel.data.mapper.toData
import com.catedra.bitacora.features.travel.data.mapper.toDomain
import com.catedra.bitacora.features.travel.data.mapper.toPointOfInterest
import com.catedra.bitacora.features.travel.data.mapper.toPointsDomain
import com.catedra.bitacora.features.travel.data.mapper.toTravel
import com.catedra.bitacora.features.travel.data.remote.TravelRemoteDataSource
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class TravelRepositoryFirebase @Inject constructor(
    private val remoteDataSource: TravelRemoteDataSource
) : TravelsRepository {

    override suspend fun getTravels(userId: String, page: Int): Result<List<Travel>> {
        return try {
            val documents = remoteDataSource.getTravels(userId)
            val travels = documents.toDomain()
            Result.success(travels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTravelById(travelId: String): Result<Travel> {
        return try {
            val doc = remoteDataSource.getTravelById(travelId)
            val travel = doc.toTravel()
            if (travel != null) {
                Result.success(travel)
            } else {
                Result.failure(Exception("Viaje no encontrado o error al procesar datos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPointsOfInterest(travelId: String): Result<List<PointOfInterest>> {
        return try {
            val querySnapshot = remoteDataSource.getPointsOfInterest(travelId)
            Result.success(querySnapshot.toPointsDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPointOfInterest(travelId: String, pointId: String): Result<PointOfInterest> {
        return try {
            val doc = remoteDataSource.getPointOfInterest(travelId, pointId)
            val point = doc.toPointOfInterest()
            Result.success(point)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPointsCount(travelId: String): Result<Int> {
        return try {
            val count = remoteDataSource.getPointsCount(travelId)
            Result.success(count.toInt())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveTravel(travel: Travel): Result<String> {
        return try {
            val travelData = travel.toData()
            val travelId = remoteDataSource.saveTravel(travelData)
            Result.success(travelId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePoint(
        travelId: String,
        point: PointOfInterest,
        geohash: String?,
        authorizedUsers: List<String>
    ): Result<String> {
        return try {
            val pointData = point.toData().toMutableMap()
            if (geohash != null) pointData["geohash"] = geohash
            if (authorizedUsers.isNotEmpty()) pointData["authorizedUsers"] = authorizedUsers
            val pointId = remoteDataSource.savePoint(travelId, pointData)
            Result.success(pointId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTravel(travel: Travel): Result<Unit> {
        return try {
            remoteDataSource.updateTravel(travel.id, travel.toData())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePoint(
        travelId: String,
        point: PointOfInterest,
        geohash: String?,
        authorizedUsers: List<String>
    ): Result<Unit> {
        return try {
            val data = point.toData().toMutableMap()
            if (geohash != null) data["geohash"] = geohash
            if (authorizedUsers.isNotEmpty()) data["authorizedUsers"] = authorizedUsers
            remoteDataSource.updatePoint(travelId, point.id, data)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePoint(travelId: String, pointId: String): Result<Unit> {
        return try {
            remoteDataSource.deletePoint(travelId, pointId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTripAccess(travelId: String, newPrivileges: List<String>, removed: List<String>): Result<Unit> {
        return try {
            remoteDataSource.syncTripAccess(travelId, newPrivileges, removed)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun getSharedTravels(userId: String): Result<List<Travel>> {
        return try {
            val tripIds = remoteDataSource.getSharedTripIds(userId)
            val travels = tripIds.mapNotNull { tripId ->
                remoteDataSource.getTravelById(tripId).toTravel()
            }.sortedByDescending { it.updatedAt }
            Result.success(travels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}