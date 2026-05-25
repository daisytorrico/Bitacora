package com.catedra.bitacora.features.travel.domain.repository

import com.catedra.bitacora.features.travel.domain.model.Travel

interface TravelsRepository {
    suspend fun getTravels( userId: String, page: Int): Result<List<Travel>>;

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
    }
}