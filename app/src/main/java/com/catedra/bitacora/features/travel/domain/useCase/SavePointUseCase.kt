package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.features.map.data.util.GeohashUtils
import com.catedra.bitacora.features.travel.domain.model.PointOfInterest
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class SavePointUseCase @Inject constructor(
    private val travelsRepository: TravelsRepository
) {
    suspend operator fun invoke(travelId: String, point: PointOfInterest): Result<String> {
        val travelResult = travelsRepository.getTravelById(travelId)
        val travel = travelResult.getOrNull() ?: return Result.failure(Exception("Viaje no encontrado"))

        // Validación de fechas (Punto 5)
        point.visitDate?.let { visitDateTime ->
            val visitDate = visitDateTime.toLocalDate()
            if (travel.startDate != null && visitDate.isBefore(travel.startDate)) {
                return Result.failure(Exception("La fecha de visita no puede ser anterior al inicio del viaje"))
            }
            if (travel.endDate != null && visitDate.isAfter(travel.endDate)) {
                return Result.failure(Exception("La fecha de visita no puede ser posterior al fin del viaje"))
            }
        }

        // Recolectar todos los usuarios autorizados (dueño + privilegios)
        val authorizedUsers = mutableSetOf(travel.ownerId)
        travel.privileges?.keys?.let { authorizedUsers.addAll(it) }

        // Calcular geohash si hay coordenadas
        val geohash = if (point.latitude != null && point.longitude != null) {
            GeohashUtils.encode(point.latitude, point.longitude)
        } else null

        return travelsRepository.savePoint(travelId, point, geohash, authorizedUsers.toList())
    }
}
