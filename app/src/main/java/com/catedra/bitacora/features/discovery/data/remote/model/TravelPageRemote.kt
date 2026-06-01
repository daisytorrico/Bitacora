package com.catedra.bitacora.features.discovery.data.remote.model

import com.catedra.bitacora.features.travel.domain.model.Travel
import com.google.firebase.firestore.DocumentSnapshot

data class TravelPageRemote(
    val travels: List<Travel>,
    val lastDocument: DocumentSnapshot?
)
