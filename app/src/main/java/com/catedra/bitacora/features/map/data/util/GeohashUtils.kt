package com.catedra.bitacora.features.map.data.util

import com.catedra.bitacora.core.domain.model.Coordinates
import kotlin.math.*

object GeohashUtils {
    private const val BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"

    /**
     * Codifica coordenadas en un Geohash de precisión específica.
     * Para Firestore, 10 caracteres son ~1 metro. 5 caracteres son ~5km.
     */
    fun encode(lat: Double, lng: Double, precision: Int = 10): String {
        val latRange = doubleArrayOf(-90.0, 90.0)
        val lngRange = doubleArrayOf(-180.0, 180.0)
        var isEven = true
        var bit = 0
        var ch = 0
        val geohash = StringBuilder()

        while (geohash.length < precision) {
            val mid: Double
            if (isEven) {
                mid = (lngRange[0] + lngRange[1]) / 2
                if (lng > mid) {
                    ch = ch or (1 shl (4 - bit))
                    lngRange[0] = mid
                } else {
                    lngRange[1] = mid
                }
            } else {
                mid = (latRange[0] + latRange[1]) / 2
                if (lat > mid) {
                    ch = ch or (1 shl (4 - bit))
                    latRange[0] = mid
                } else {
                    latRange[1] = mid
                }
            }

            isEven = !isEven
            if (bit < 4) {
                bit++
            } else {
                geohash.append(BASE32[ch])
                bit = 0
                ch = 0
            }
        }
        return geohash.toString()
    }

    /**
     * Calcula el rango de geohashes para una búsqueda circular.
     * Firestore permite queries de rango: geohash >= start AND geohash <= end.
     */
    fun getSearchRange(lat: Double, lng: Double, radiusKm: Double): Pair<String, String> {
        // Reducimos precisión según el radio para cubrir el área
        val precision = when {
            radiusKm <= 0.05 -> 9 // 50m
            radiusKm <= 0.5 -> 7  // 500m
            radiusKm <= 5.0 -> 5  // 5km
            radiusKm <= 50.0 -> 4 // 50km
            else -> 3
        }
        val hash = encode(lat, lng, precision)
        return Pair(hash, hash + "\uf8ff")
    }

    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
