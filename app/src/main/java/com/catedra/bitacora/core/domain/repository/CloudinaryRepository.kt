package com.catedra.bitacora.core.domain.repository

import android.net.Uri

interface CloudinaryRepository {
    suspend fun uploadImage(uri: Uri): String
}