package com.catedra.bitacora.features.social.data.mapper

import com.catedra.bitacora.features.social.data.model.CommentData
import com.catedra.bitacora.features.social.domain.model.Comment
import java.time.LocalDateTime
import java.time.ZoneId

fun CommentData.toDomain(): Comment = Comment(
    id = id,
    userId = userId,
    username = username,
    userPhotoUrl = userPhotoUrl,
    content = content,
    timestamp = timestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime() ?: LocalDateTime.now(),
    likesCount = likesCount,
    parentId = parentId
)

fun List<CommentData>.toDomain(): List<Comment> = map { it.toDomain() }
