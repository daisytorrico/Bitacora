package com.catedra.bitacora.features.travel.domain.useCase

import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import javax.inject.Inject

class GetTravelsListUseCase @Inject constructor(
    private val repository: TravelsRepository
) {
    suspend operator fun invoke(page: Int): Result<List<Travel>> {
        return repository.getTravels("TODO", page);
    }
}