package com.catedra.bitacora.core.domain.model

data class ExternalPoi(
    val id: String,
    val travelId: String,
    override val name: String,
    override val address: String,
    override val coordinates: Coordinates
) : PointOnMap(name, address, coordinates, null)
