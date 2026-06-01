package com.catedra.bitacora.features.discovery.domain.model

import com.catedra.bitacora.features.travel.domain.model.Travel

data class TravelPage(
    val travels: List<Travel>,
    val lastDocument: Any?
)
