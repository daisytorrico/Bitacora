package com.catedra.bitacora.features.travel.data.remote

import com.catedra.bitacora.features.travel.domain.model.Travel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import javax.inject.Inject
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class TravelRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    suspend fun getTravelDocument(): List<Travel> {
        val uid = auth.currentUser?.uid ?: throw Exception("Usuario no loggeado");

        // TODO: Acceder a Firebase y Cloudify

        return listOf(
            Travel("id1", "Cancún", LocalDate.of(2026, 2, 2), LocalDate.of(2025, 3, 3)),
            Travel("id2", "París", LocalDate.of(2026, 4, 4), LocalDate.of(2025, 5, 5)),
            Travel("id3", "Algún otro lugar", LocalDate.of(2026, 6, 6), LocalDate.of(2025, 7, 7)),
            Travel("id4", "Prueba", LocalDate.of(2026, 8, 8), LocalDate.of(2025, 9, 9))
        )
    }
}