package com.catedra.bitacora.features.auth.domain.model

data class User(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val username: String? = null
)
