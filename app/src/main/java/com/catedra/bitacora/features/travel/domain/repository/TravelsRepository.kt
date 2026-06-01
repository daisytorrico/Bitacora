package com.catedra.bitacora.features.travel.domain.repository

import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.model.Travel

interface TravelsRepository {
    suspend fun getTravels(userId: String, page: Int): Result<List<Travel>>
    suspend fun getTravelById(travelId: String): Result<Travel>
    suspend fun getPointsOfInterest(travelId: String): Result<List<PointOfInterest>>
    suspend fun getPointOfInterest(travelId: String, pointId: String): Result<PointOfInterest>
    suspend fun getPointsCount(travelId: String): Result<Int>
    suspend fun savePoint(
        travelId: String,
        point: PointOfInterest,
        geohash: String? = null,
        authorizedUsers: List<String> = emptyList()
    ): Result<String>


    // Guardar el viaje y retornar su ID
    suspend fun saveTravel(travel: Travel): Result<String>

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
    }
}
