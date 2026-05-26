package com.catedra.bitacora.core.data.mapper

import android.location.Address
import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap

/**
 * Convierte una lista de direcciones en el mejor punto posible para el mapa.
 * Prioriza nombres de lugares (POIs) sobre direcciones numéricas.
 */
fun List<Address>.toBestDomain(coordinates: Coordinates): PointOnMap? {
    if (isEmpty()) return null

    // 1. Intentar encontrar un POI (donde el nombre no sea igual a la calle o altura)
    val poiAddress = firstOrNull { address ->
        val feature = address.featureName
        val street = address.thoroughfare
        feature != null && feature != street && !feature.isPurelyNumeric()
    }

    // 2. Si no hay POI, intentar encontrar una dirección con calle y número
    val addressWithStreet = poiAddress ?: firstOrNull { it.thoroughfare != null }

    // 3. Mapear el resultado seleccionado
    return (addressWithStreet ?: first()).toDomain(coordinates)
}

fun Address.toDomain(coordinates: Coordinates): PointOnMap {
    val isPOI = featureName != null && featureName != thoroughfare && !featureName.isPurelyNumeric()
    
    val name = if (isPOI) {
        featureName!!
    } else {
        buildStreetAddress() ?: getAddressLine(0) ?: "Punto seleccionado"
    }

    val address = if (isPOI) {
        buildStreetAddress() ?: getAddressLine(0) ?: ""
    } else {
        // Para no-POIs, la dirección es el resto de la info (ciudad, país)
        buildLocationDetails()
    }

    return PointOnMap(
        name = name,
        address = address,
        coordinates = coordinates
    )
}

private fun Address.buildStreetAddress(): String? {
    val street = thoroughfare ?: return null
    val number = if (featureName?.isPurelyNumeric() == true) featureName else subThoroughfare
    return if (number != null) "$street $number" else street
}

private fun Address.buildLocationDetails(): String {
    val city = locality ?: adminArea
    val country = countryName
    return listOfNotNull(city, country).joinToString(", ")
}

private fun String.isPurelyNumeric(): Boolean = this.all { it.isDigit() }
