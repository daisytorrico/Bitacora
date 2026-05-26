package com.catedra.bitacora.features.map.data.remote

import android.content.Context
import android.location.Geocoder
import com.catedra.bitacora.features.map.domain.model.PointOnMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

class GeocodingRemoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun getPointFromCoordinates(latitude: Double, longitude: Double): PointOnMap? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val name = address.featureName ?: address.getAddressLine(0) ?: "Punto seleccionado"
                PointOnMap(
                    name = name,
                    latitude = latitude,
                    longitude = longitude
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
