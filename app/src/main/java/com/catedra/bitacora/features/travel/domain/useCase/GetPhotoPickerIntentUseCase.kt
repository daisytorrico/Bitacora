package com.catedra.bitacora.features.travel.domain.useCase

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.catedra.bitacora.features.travel.domain.repository.ImageRepository
import javax.inject.Inject

class GetPhotoPickerIntentUseCase @Inject constructor(
    private val imageRepository: ImageRepository
) {
    operator fun invoke(title: String, allowMultiple: Boolean = false): Pair<Intent, Uri> {
        val tempUri = imageRepository.createTempPictureUri()

        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, tempUri)
        }

        val pickIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            if (allowMultiple) putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        val chooserIntent = Intent.createChooser(pickIntent, title).apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(captureIntent))
        }

        return Pair(chooserIntent, tempUri)
    }
}