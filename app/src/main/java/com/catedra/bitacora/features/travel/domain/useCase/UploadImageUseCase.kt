package com.catedra.bitacora.features.travel.domain.useCase

import android.net.Uri
import com.catedra.bitacora.features.travel.data.remote.CloudinaryDataSource
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val cloudinaryDataSource: CloudinaryDataSource
) {
    suspend operator fun invoke(uri: Uri): String {
        return cloudinaryDataSource.uploadImage(uri)
    }
}
