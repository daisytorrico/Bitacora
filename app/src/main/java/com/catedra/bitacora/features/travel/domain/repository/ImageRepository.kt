package com.catedra.bitacora.features.travel.domain.repository

import android.net.Uri

interface ImageRepository {
    fun createTempPictureUri(): Uri
    suspend fun compressImage(uri: Uri): Uri?
}