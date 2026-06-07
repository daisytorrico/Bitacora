package com.catedra.bitacora.features.discovery.presentation.publicProfile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.core.domain.repository.SessionRepository
import com.catedra.bitacora.features.discovery.domain.useCase.GetPublicProfileUseCase
import com.catedra.bitacora.features.social.domain.useCase.FollowUseCases
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
    private val sessionRepository: SessionRepository,
    private val useCase: GetPublicProfileUseCase,
    private val followUseCases: FollowUseCases
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

            val myUid = sessionRepository.getCurrentUser()?.uid
            val isMe = userId == myUid

            val userResult = useCase.getUser(userId)
            val travelsResult = useCase.getTravels(userId)
            val followResult = if (isMe) Result.success(false) else followUseCases.isFollowing(userId)

            _uiState.update { it.copy(
                user = userResult.getOrNull(),
                travels = travelsResult.getOrDefault(emptyList()),
                isFollowing = followResult.getOrDefault(false),
                isMe = isMe,
                isLoading = false,
                error = userResult.exceptionOrNull()?.message
            ) }
        }
    }

    fun toggleFollow() {
        viewModelScope.launch {
            val currentIsFollowing = uiState.value.isFollowing
            val result = followUseCases.toggleFollow(userId, currentIsFollowing)

            if (result.isSuccess) {
                val currentUser = uiState.value.user
                if (currentUser != null) {
                    val newFollowersCount = if (currentIsFollowing) {
                        (currentUser.followersCount - 1).coerceAtLeast(0)
                    } else {
                        currentUser.followersCount + 1
                    }

                    _uiState.update { it.copy(
                        isFollowing = !currentIsFollowing,
                        user = currentUser.copy(followersCount = newFollowersCount),
                        followMessage = if (currentIsFollowing) "Dejaste de seguir a @${currentUser.username}" else "Ahora sigues a @${currentUser.username}"
                    ) }

                    loadProfile()
                }
            }
        }
    }

    fun resetFollowMessage() {
        _uiState.update { it.copy(followMessage = null) }
    }
}
