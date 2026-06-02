package com.catedra.bitacora.core.domain.model

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val username: String? = null,
    val photoUrl: String? = null,
    val bio: String? = null,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)
