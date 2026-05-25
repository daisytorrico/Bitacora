package com.catedra.bitacora.features.travel.data.repository

import com.catedra.bitacora.features.travel.data.mapper.toData
import com.catedra.bitacora.features.travel.data.mapper.toDomain
import com.catedra.bitacora.features.travel.data.remote.TravelRemoteDataSource
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class TravelRepositoryFirebase @Inject constructor(
    private val remoteDataSource: TravelRemoteDataSource
) : TravelsRepository {

    override suspend fun getTravels(userId: String, page: Int): Result<List<Travel>> {
        return try {
            val querySnapshot = remoteDataSource.getTravels(userId)
            val travels = querySnapshot.toDomain()
            Result.success(travels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveTravel(travel: Travel): Result<Unit> {
        return try {
            val travelData = travel.toData()
            remoteDataSource.saveTravel(travelData)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
