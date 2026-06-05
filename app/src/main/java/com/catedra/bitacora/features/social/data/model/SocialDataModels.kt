package com.catedra.bitacora.features.social.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class CommentData(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userPhotoUrl: String? = null,
    val content: String = "",
    val timestamp: Timestamp? = null,
    val likesCount: Int = 0,
    val parentId: String? = null
)
