package com.catedra.bitacora.features.social.domain.model

import java.time.LocalDateTime

data class Comment(
    val id: String = "",
    val userId: String,
    val username: String,
    val userPhotoUrl: String?,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val likesCount: Int = 0
)

data class Reply(
    val id: String = "",
    val userId: String,
    val username: String,
    val userPhotoUrl: String?,
    val content: String,
    val timestamp: LocalDateTime = LocalDateTime.now()
)
