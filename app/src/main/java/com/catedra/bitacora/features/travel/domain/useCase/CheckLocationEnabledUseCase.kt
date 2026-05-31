package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.features.travel.domain.repository.LocationRepository
import javax.inject.Inject

class CheckLocationEnabledUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    operator fun invoke(): Boolean = repository.isLocationEnabled()
}
