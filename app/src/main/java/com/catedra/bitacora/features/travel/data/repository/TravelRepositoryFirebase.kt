package com.catedra.bitacora.features.travel.data.repository

import com.catedra.bitacora.features.travel.data.mapper.toDomain
import com.catedra.bitacora.features.travel.data.remote.TravelRemoteDataSource
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class TravelRepositoryFirebase @Inject constructor(
    private val remoteDataSource: TravelRemoteDataSource
) : TravelsRepository {
    override suspend fun getTravels( userId: String, page: Int): Result<List<Travel>> {
        return try {
            val result = remoteDataSource.getTravelDocument()
            Result.success(result);
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}