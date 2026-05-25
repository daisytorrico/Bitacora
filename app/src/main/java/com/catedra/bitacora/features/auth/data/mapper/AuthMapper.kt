package com.catedra.bitacora.features.auth.data.mapper

import com.catedra.bitacora.features.auth.domain.model.User
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toDomain(): User {
    return User(
        uid = uid,
        email = email,
        displayName = displayName
    )
}
