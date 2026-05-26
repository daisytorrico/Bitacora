package com.catedra.bitacora.core.data.repository

import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.data.remote.GeocodingRemoteDataSource
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.repository.GeocodingRepository
import javax.inject.Inject

class GeocodingRepositoryImpl @Inject constructor(
    private val remoteDataSource: GeocodingRemoteDataSource
) : GeocodingRepository {
    override suspend fun getPointFromCoordinates(coordinates: Coordinates): Result<PointOnMap> {
        return try {
            val point = remoteDataSource.getPointFromCoordinates(coordinates)
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
