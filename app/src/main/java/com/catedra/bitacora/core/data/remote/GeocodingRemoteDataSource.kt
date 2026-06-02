package com.catedra.bitacora.core.data.remote

import android.location.Geocoder
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.data.mapper.toBestDomain
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
            val addresses = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 5)
            
            addresses?.toBestDomain(coordinates)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchLocation(query: String): List<PointOnMap> = withContext(Dispatchers.IO) {
        try {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 5)
            addresses?.map { address ->
                address.toDomain(Coordinates(address.latitude, address.longitude))
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
