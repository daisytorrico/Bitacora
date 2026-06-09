package com.catedra.bitacora.core.domain.model

import com.catedra.bitacora.core.ui.util.UiText

open class PointOnMap(
    open val name: String,
    open val address: String,
    open val coordinates: Coordinates,
    open val nameUiText: UiText? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PointOnMap) return false
        if (name != other.name) return false
        if (address != other.address) return false
        if (coordinates != other.coordinates) return false
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + coordinates.hashCode()
        return result
    }
}
