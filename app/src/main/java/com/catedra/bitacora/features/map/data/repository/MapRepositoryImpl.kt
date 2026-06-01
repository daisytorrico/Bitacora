package com.catedra.bitacora.features.map.data.repository

import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.features.map.data.mapper.toPointOnMap
import com.catedra.bitacora.features.map.data.remote.MapRemoteDataSource
import com.catedra.bitacora.features.map.data.util.GeohashUtils
import com.catedra.bitacora.features.map.domain.repository.MapRepository
import javax.inject.Inject

class MapRepositoryImpl @Inject constructor(
    private val remoteDataSource: MapRemoteDataSource
) : MapRepository {

    override suspend fun getNearbyPoints(
        userId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<PointOnMap>> {
        return try {
            // 1. Obtener el rango de geohashes para la query
            val range = GeohashUtils.getSearchRange(latitude, longitude, radiusKm)
            
            // 2. Query eficiente en Firestore
            val snapshot = remoteDataSource.getNearbyPointsByGeohash(userId, range)
            
            // 3. Mapeo y filtro circular final (el geohash devuelve un cuadrado aproximado)
            val points = snapshot.documents.map { it.toPointOnMap() }
                .filter { point ->
                    GeohashUtils.calculateDistanceKm(
                        latitude, longitude,
                        point.coordinates.latitude, point.coordinates.longitude
                    ) <= radiusKm
                }
            
            Result.success(points)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
