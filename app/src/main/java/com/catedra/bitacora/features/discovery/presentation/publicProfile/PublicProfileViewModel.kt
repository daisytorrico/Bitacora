package com.catedra.bitacora.features.discovery.presentation.publicProfile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.features.discovery.domain.useCase.GetPublicProfileUseCase
import com.catedra.bitacora.features.social.domain.useCase.FollowUserUseCase
import com.catedra.bitacora.features.social.domain.useCase.GetIsFollowingUseCase
import com.catedra.bitacora.features.social.domain.useCase.UnfollowUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val useCase: GetPublicProfileUseCase,
    private val followUseCase: FollowUserUseCase,
    private val unfollowUseCase: UnfollowUserUseCase,
    private val isFollowingUseCase: GetIsFollowingUseCase
) : ViewModel() {
    private val userId: String = checkNotNull(savedStateHandle["userId"])

    private val _uiState = MutableStateFlow(PublicProfileUiState())
    val uiState: StateFlow<PublicProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val userResult = useCase.getUser(userId)
            val travelsResult = useCase.getTravels(userId)
            val followResult = isFollowingUseCase(userId)

            _uiState.update { it.copy(
                user = userResult.getOrNull(),
                travels = travelsResult.getOrDefault(emptyList()),
                isFollowing = followResult.getOrDefault(false),
                isLoading = false,
                error = userResult.exceptionOrNull()?.message
            ) }
        }
    }

    fun toggleFollow() {
        viewModelScope.launch {
            val currentIsFollowing = uiState.value.isFollowing
            val result = if (currentIsFollowing) unfollowUseCase(userId) else followUseCase(userId)

            if (result.isSuccess) {
                // Actualizamos el estado local para que el contador cambie al instante
                val currentUser = uiState.value.user
                if (currentUser != null) {
                    val newFollowersCount = if (currentIsFollowing) {
                        (currentUser.followersCount - 1).coerceAtLeast(0)
                    } else {
                        currentUser.followersCount + 1
                    }
                    
                    _uiState.update { it.copy(
                        isFollowing = !currentIsFollowing,
                        user = currentUser.copy(followersCount = newFollowersCount)
                    ) }
                }
            }
        }
    }
}
