package com.catedra.bitacora.core.data.repository

import com.catedra.bitacora.core.data.mapper.toPointOnMap
import com.catedra.bitacora.core.data.remote.NearbyPointsRemoteDataSource
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.repository.NearbyPointsRepository
import com.catedra.bitacora.core.utils.GeohashUtils
import javax.inject.Inject

class NearbyPointsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NearbyPointsRemoteDataSource
) : NearbyPointsRepository {

    override suspend fun getNearbyPoints(
        userId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<PointOnMap>> {
        return try {
            val range = GeohashUtils.getSearchRange(latitude, longitude, radiusKm)
            val snapshot = remoteDataSource.getNearbyPointsByGeohash(userId, range)
            
            val points = snapshot.documents.map { it.toPointOnMap() }
                .filter { point ->
                    GeohashUtils.calculateDistanceKm(
                        latitude, longitude,
                        point.coordinates.latitude, point.coordinates.longitude
                    ) <= radiusKm
                }
            
            Result.success(points)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
