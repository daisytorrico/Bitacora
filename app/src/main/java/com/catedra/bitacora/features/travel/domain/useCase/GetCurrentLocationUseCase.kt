package com.catedra.bitacora.features.travel.domain.useCase

import android.location.Location
import com.catedra.bitacora.features.travel.domain.model.UserLocation
import com.catedra.bitacora.features.travel.domain.repository.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    suspend operator fun invoke(): Result<UserLocation> {
        return locationRepository.getCurrentLocation().mapCatching { location ->
            location?.let {
                val address = locationRepository.getAddressFromLocation(it.latitude, it.longitude)
                UserLocation(it.latitude, it.longitude, address)
            } ?: throw Exception("No se pudo obtener la ubicación GPS")
        }
    }
}
