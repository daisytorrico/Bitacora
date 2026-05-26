package com.catedra.bitacora.core.data.remote

import android.location.Geocoder
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.data.mapper.toDomain
import com.catedra.bitacora.core.domain.model.PointOnMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GeocodingRemoteDataSource @Inject constructor(
    private val geocoder: Geocoder
) {
    suspend fun getPointFromCoordinates(coordinates: Coordinates): PointOnMap? = withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                addresses[0].toDomain(coordinates)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
