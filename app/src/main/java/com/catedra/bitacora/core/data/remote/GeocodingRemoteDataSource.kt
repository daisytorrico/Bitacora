package com.catedra.bitacora.core.data.remote

import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.data.mapper.toBestDomain
import com.catedra.bitacora.core.data.mapper.toDomain
import com.catedra.bitacora.core.domain.model.PointOnMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class GeocodingRemoteDataSource @Inject constructor(
    private val geocoder: Geocoder
) {
    suspend fun getPointFromCoordinates(coordinates: Coordinates): PointOnMap? = withContext(Dispatchers.IO) {
        try {
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 5, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: List<Address>) {
                            continuation.resume(addresses)
                        }
                        override fun onError(errorMessage: String?) {
                            continuation.resume(null)
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 5)
            }
            
            addresses?.toBestDomain(coordinates)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchLocation(query: String): List<PointOnMap> = withContext(Dispatchers.IO) {
        try {
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocationName(query, 5, object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: List<Address>) {
                            continuation.resume(addresses)
                        }
                        override fun onError(errorMessage: String?) {
                            continuation.resume(null)
                        }
                    })
                }
            } else {
                @Suppress("DEPRECATION")
                geocoder.getFromLocationName(query, 5)
            }

            addresses?.map { address ->
                address.toDomain(Coordinates(address.latitude, address.longitude))
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
