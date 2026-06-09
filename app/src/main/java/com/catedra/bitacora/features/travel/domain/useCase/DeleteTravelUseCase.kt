package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.core.domain.useCase.CancelPointNotificationUseCase
import com.catedra.bitacora.core.domain.useCase.CancelTravelNotificationUseCase
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class DeleteTravelUseCase @Inject constructor(
    private val repository: TravelsRepository,
    private val cancelPointNotificationUseCase: CancelPointNotificationUseCase,
    private val cancelTravelNotificationUseCase: CancelTravelNotificationUseCase
) {
    suspend operator fun invoke(travelId: String): Result<Unit> {
        repository.getPointsOfInterest(travelId).onSuccess { points ->
            points.forEach { point ->
                cancelPointNotificationUseCase(point.id)
            }
        }
        cancelTravelNotificationUseCase(travelId)
        return repository.deleteTravel(travelId)
    }
}
