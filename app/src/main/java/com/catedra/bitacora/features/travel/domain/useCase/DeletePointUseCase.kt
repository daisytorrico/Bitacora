package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.core.domain.useCase.CancelPointNotificationUseCase
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class DeletePointUseCase @Inject constructor(
    private val repository: TravelsRepository,
    private val cancelPointNotificationUseCase: CancelPointNotificationUseCase
) {
    suspend operator fun invoke(travelId: String, pointId: String): Result<Unit> {
        cancelPointNotificationUseCase(pointId)
        return repository.deletePoint(travelId, pointId)
    }
}
