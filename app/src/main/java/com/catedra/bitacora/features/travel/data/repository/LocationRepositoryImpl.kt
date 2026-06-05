package com.catedra.bitacora.features.travel.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import com.catedra.bitacora.features.travel.domain.repository.LocationRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationRepository {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<Location?> {
        return try {
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()
            Result.success(location)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAddressFromLocation(latitude: Double, longitude: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            
            val addresses = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1, object : Geocoder.GeocodeListener {
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
                geocoder.getFromLocation(latitude, longitude, 1)
            }

            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val street = addr.thoroughfare ?: ""
                val number = addr.subThoroughfare ?: ""
                val city = addr.locality ?: ""
                if (street.isNotEmpty()) "$street $number, $city".trim()
                else String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
            } else {
                String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
            }
        } catch (e: Exception) {
            String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
        }
    }
}
