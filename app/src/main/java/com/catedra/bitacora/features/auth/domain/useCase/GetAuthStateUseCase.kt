package com.catedra.bitacora.features.auth.domain.useCase

import com.catedra.bitacora.core.domain.model.AuthState
import com.catedra.bitacora.features.auth.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GetAuthStateUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): StateFlow<AuthState> {
        return repository.authState
    }
}
