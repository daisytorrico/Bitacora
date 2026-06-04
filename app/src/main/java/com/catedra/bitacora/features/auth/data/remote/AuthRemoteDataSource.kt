package com.catedra.bitacora.features.auth.data.remote

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRemoteDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun addAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.addAuthStateListener(listener)
    }

    fun removeAuthStateListener(listener: FirebaseAuth.AuthStateListener) {
        auth.removeAuthStateListener(listener)
    }

    suspend fun signInWithEmail(email: String, pass: String): AuthResult {
        return auth.signInWithEmailAndPassword(email, pass).await()
    }

    suspend fun createUserWithEmail(email: String, pass: String): AuthResult {
        return auth.createUserWithEmailAndPassword(email, pass).await()
    }

    suspend fun signInWithCredential(credential: AuthCredential): AuthResult {
        return auth.signInWithCredential(credential).await()
    }

    suspend fun updateProfile(updates: UserProfileChangeRequest) {
        auth.currentUser?.updateProfile(updates)?.await()
    }

    suspend fun reloadUser() {
        auth.currentUser?.reload()?.await()
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun getUserDocument(uid: String): DocumentSnapshot {
        return db.collection("users").document(uid).get().await()
    }

    suspend fun checkUsernameUniqueness(username: String): Boolean {
        val query = db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .await()
        return query.isEmpty
    }

    suspend fun saveUserDocument(uid: String, data: Map<String, Any?>) {
        db.collection("users").document(uid).set(data).await()
    }

    suspend fun searchUsers(query: String): List<DocumentSnapshot> {
        val q = query.lowercase()
        return db.collection("users")
            .whereGreaterThanOrEqualTo("username", q)
            .whereLessThanOrEqualTo("username", q + "\uf8ff")
            .limit(10)
            .get()
            .await()
            .documents
    }

    suspend fun getUsersByIds(uids: List<String>): List<DocumentSnapshot> {
        if (uids.isEmpty()) return emptyList()
        return db.collection("users")
            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), uids)
            .get()
            .await()
            .documents
    }
}
