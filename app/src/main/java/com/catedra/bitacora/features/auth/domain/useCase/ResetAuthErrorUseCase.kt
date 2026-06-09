package com.catedra.bitacora.features.auth.domain.useCase

import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import javax.inject.Inject

class ResetAuthErrorUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke() {
        repository.resetError()
    }
}
