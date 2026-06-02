package com.catedra.bitacora.features.auth.data.mapper

import com.catedra.bitacora.core.domain.model.User
import com.google.firebase.auth.FirebaseUser

import com.google.firebase.firestore.DocumentSnapshot

fun FirebaseUser.toDomain(): User {
    return User(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString()
    )
}

fun DocumentSnapshot.toUser(): User {
    return User(
        uid = id,
        email = getString("email"),
        displayName = getString("nombre") ?: getString("displayName"),
        username = getString("username"),
        photoUrl = getString("photoUrl"),
        bio = getString("bio"),
        followersCount = getLong("followersCount")?.toInt() ?: 0,
        followingCount = getLong("followingCount")?.toInt() ?: 0
    )
}

fun User.toData(): Map<String, Any?> {
    return hashMapOf(
        "nombre" to displayName,
        "email" to email,
        "username" to username,
        "photoUrl" to photoUrl,
        "bio" to bio,
        "followersCount" to followersCount,
        "followingCount" to followingCount
    )
}
