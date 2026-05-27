package com.catedra.bitacora.features.travel.data.repository

import android.util.Log
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
            val querySnapshot = remoteDataSource.getTravels(userId)
            val travels = querySnapshot.toDomain()
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

    override suspend fun saveTravel(travel: Travel): Result<Unit> {
        return try {
            val travelData = travel.toData()
            Log.d("TravelRepository", "Guardando viaje: $travelData")
            remoteDataSource.saveTravel(travelData)
            Log.d("TravelRepository", "Viaje guardado exitosamente")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("TravelRepository", "Error al guardar viaje", e)
            Result.failure(e)
        }
    }
}
