package com.catedra.bitacora.features.travel.presentation.travelList

import com.catedra.bitacora.core.domain.model.User
import com.catedra.bitacora.features.travel.domain.model.Travel
import com.catedra.bitacora.features.travel.domain.model.TravelStatus
import com.catedra.bitacora.features.travel.domain.model.TravelVisibility
data class Language(
    val name: String,
    val code: String
)

val languages = listOf(
    Language("Español", "es"),
    Language("English", "en"),
    Language("Italiano", "it")
)
data class TravelListUiState(
    val user: User? = null,
    val searchQuery: String = "",
    val myTravels: List<Travel> = emptyList(),
    val sharedTravels: List<Travel> = emptyList(),
    val page: Int = 0,
    val loading: Boolean = true,
    val selectedStatus: TravelStatus? = null,
    val selectedVisibility: TravelVisibility? = null,
    val selectedLanguage: Language = languages.first()
) {
    val filteredMyTravels: List<Travel>
        get() = filterTravels(myTravels)

    val filteredSharedTravels: List<Travel>
        get() = filterTravels(sharedTravels)

    private fun filterTravels(travels: List<Travel>): List<Travel> {
        val filtered = travels.filter { travel ->
            val matchesSearch = searchQuery.isBlank() || travel.name.contains(searchQuery, ignoreCase = true)
            val matchesStatus = selectedStatus == null || travel.status == selectedStatus
            val matchesVisibility = selectedVisibility == null || travel.visibility == selectedVisibility
            
            matchesSearch && matchesStatus && matchesVisibility
        }

        return when (selectedStatus) {
            TravelStatus.PLANNED -> filtered.sortedBy { it.startDate ?: java.time.LocalDate.MAX }
            TravelStatus.ONGOING -> filtered.sortedBy { it.endDate ?: java.time.LocalDate.MAX }
            TravelStatus.COMPLETED -> filtered.sortedByDescending { it.endDate ?: java.time.LocalDate.MIN }
            else -> filtered // Mantiene el orden por updatedAt que ya viene de Firebase
        }
    }

    val isFilterActive: Boolean
        get() = selectedStatus != null || selectedVisibility != null
}
