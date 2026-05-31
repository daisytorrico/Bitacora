package com.catedra.bitacora.features.travel.domain.repository

import android.location.Location

interface LocationRepository {
    suspend fun getCurrentLocation(): Result<Location?>
    suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String
    fun isLocationEnabled(): Boolean
}
