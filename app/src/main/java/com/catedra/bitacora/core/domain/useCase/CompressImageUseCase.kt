package com.catedra.bitacora.core.domain.useCase

import android.net.Uri
import com.catedra.bitacora.core.domain.repository.ImageRepository
import javax.inject.Inject

class CompressImageUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    suspend operator fun invoke(uri: Uri): Uri? {
        return imageRepository.compressImage(uri)
    }
}