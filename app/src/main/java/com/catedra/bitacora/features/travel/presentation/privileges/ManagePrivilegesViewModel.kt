package com.catedra.bitacora.features.travel.presentation.privileges

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.features.auth.domain.useCase.GetUsersByIdsUseCase
import com.catedra.bitacora.features.auth.domain.useCase.SearchUserUseCase
import com.catedra.bitacora.features.travel.domain.repository.TravelsRepository
import com.catedra.bitacora.features.travel.domain.useCase.UpdateTravelPrivilegesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ManagePrivilegesViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val travelsRepository: TravelsRepository,
    private val getUsersByIdsUseCase: GetUsersByIdsUseCase,
    private val searchUserUseCase: SearchUserUseCase,
    private val updateTravelPrivilegesUseCase: UpdateTravelPrivilegesUseCase
) : ViewModel() {

    private val travelId: String = checkNotNull(savedStateHandle["travelId"])

    private val _uiState = MutableStateFlow(ManagePrivilegesUiState())
    val uiState: StateFlow<ManagePrivilegesUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        loadInitialData()
        setupSearch()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            travelsRepository.getTravelById(travelId).onSuccess { travel ->
                val privileges = travel.privileges ?: emptyList()
                getUsersByIdsUseCase(privileges).onSuccess { users ->
                    _uiState.update { it.copy(collaborators = users, isLoading = false) }
                }.onFailure { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    private fun setupSearch() {
        _searchQuery
            .debounce(300L)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.length < 3) {
                    _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
                    return@onEach
                }
                _uiState.update { it.copy(isSearching = true) }
                searchUserUseCase(query).onSuccess { results ->
                    val currentCollaboratorsIds = _uiState.value.collaborators.map { it.uid }
                    val filteredResults = results.filter { it.uid !in currentCollaboratorsIds }
                    _uiState.update { it.copy(searchResults = filteredResults, isSearching = false) }
                }.onFailure {
                    _uiState.update { it.copy(isSearching = false) }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun addCollaborator(user: User) {
        _uiState.update { it.copy(
            collaborators = it.collaborators + user,
            searchResults = it.searchResults - user,
            searchQuery = ""
        ) }
        _searchQuery.value = ""
    }

    fun removeCollaborator(user: User) {
        _uiState.update { it.copy(collaborators = it.collaborators - user) }
    }

    fun savePrivileges() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val uids = _uiState.value.collaborators.map { it.uid }
            updateTravelPrivilegesUseCase(travelId, uids).onSuccess {
                _uiState.update { it.copy(isSaving = false, success = true) }
            }.onFailure { e ->
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    fun resetError() = _uiState.update { it.copy(error = null) }
}
