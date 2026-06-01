package com.catedra.bitacora.features.map.domain.model

import com.catedra.bitacora.core.domain.model.Coordinates
import com.catedra.bitacora.core.domain.model.PointOnMap

data class ExternalPoi(
    val id: String,
    val travelId: String,
    override val name: String,
    override val address: String,
    override val coordinates: Coordinates
) : PointOnMap(name, address, coordinates)
