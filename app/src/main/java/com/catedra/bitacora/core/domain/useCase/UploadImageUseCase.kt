package com.catedra.bitacora.core.domain.useCase

import android.net.Uri
import com.catedra.bitacora.core.domain.repository.CloudinaryRepository
import javax.inject.Inject

class UploadImageUseCase @Inject constructor(
    private val cloudinaryRepository: CloudinaryRepository
) {
    suspend operator fun invoke(uri: Uri): String {
        return cloudinaryRepository.uploadImage(uri)
    }
}