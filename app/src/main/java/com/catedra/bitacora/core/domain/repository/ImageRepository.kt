package com.catedra.bitacora.core.domain.repository

import android.net.Uri

interface ImageRepository {
    fun createTempPictureUri(): Uri
    suspend fun compressImage(uri: Uri): Uri?
}