package com.catedra.bitacora.features.map.data.repository

import com.catedra.bitacora.features.map.data.remote.GeocodingRemoteDataSource
import com.catedra.bitacora.features.map.domain.model.PointOnMap
import com.catedra.bitacora.features.map.domain.repository.GeocodingRepository
import javax.inject.Inject

class GeocodingRepositoryImpl @Inject constructor(
    private val remoteDataSource: GeocodingRemoteDataSource
) : GeocodingRepository {
    override suspend fun getPointFromCoordinates(latitude: Double, longitude: Double): Result<PointOnMap> {
        return try {
            val point = remoteDataSource.getPointFromCoordinates(latitude, longitude)
            if (point != null) {
                Result.success(point)
            } else {
                Result.failure(Exception("No se encontró información para estas coordenadas"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
