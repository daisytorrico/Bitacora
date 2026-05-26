package com.catedra.bitacora.features.travel.domain.useCase

import android.net.Uri
import com.catedra.bitacora.features.travel.data.local.ImageManager
import javax.inject.Inject

class CompressImageUseCase @Inject constructor(
    private val imageManager: ImageManager
) {
    suspend operator fun invoke(uri: Uri): Uri? {
        return imageManager.compressImage(uri)
    }
}
