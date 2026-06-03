package com.catedra.bitacora.core.data.repository

import android.util.Log
import com.catedra.bitacora.core.data.mapper.toPointOnMap
import com.catedra.bitacora.core.data.remote.NearbyPointsRemoteDataSource
import com.catedra.bitacora.features.discovery.data.remote.DiscoveryRemoteDataSource
import com.catedra.bitacora.core.domain.model.PointOnMap
import com.catedra.bitacora.core.domain.repository.NearbyPointsRepository
import com.catedra.bitacora.core.utils.GeohashUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class NearbyPointsRepositoryImpl @Inject constructor(
    private val remoteDataSource: NearbyPointsRemoteDataSource,
    private val discoveryRemoteDataSource: DiscoveryRemoteDataSource
) : NearbyPointsRepository {

    override suspend fun getNearbyPoints(
        userId: String,
        latitude: Double,
        longitude: Double,
        radiusKm: Double
    ): Result<List<PointOnMap>> = coroutineScope {
        try {
            val range = GeohashUtils.getSearchRange(latitude, longitude, radiusKm)

            val authorizedSnapshot = async { 
                remoteDataSource.getAuthorizedNearbyPoints(userId, range) 
            }

            val publicTravels = async {
                try {
                    discoveryRemoteDataSource.getPublicTravels(limit = 20).travels
                } catch (e: Exception) {
                    emptyList()
                }
            }

            val authDocs = authorizedSnapshot.await().documents

            val travels = publicTravels.await()

            val publicPointsDocs = travels
                .filter { it.ownerId != userId }
                .map { travel ->
                    async {
                        try {
                            remoteDataSource.getPointsByTripAndGeohash(travel.id, range).documents
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                }.awaitAll().flatten()

            val allDocs = (authDocs + publicPointsDocs).distinctBy { it.id }

            val finalPoints = allDocs.mapNotNull { doc ->
                val geoPoint = doc.getGeoPoint("location") ?: return@mapNotNull null
                val distance = GeohashUtils.calculateDistanceKm(
                    latitude, longitude,
                    geoPoint.latitude, geoPoint.longitude
                )
                if (distance <= radiusKm) {
                    doc.toPointOnMap()
                } else null
            }

            Result.success(finalPoints)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
