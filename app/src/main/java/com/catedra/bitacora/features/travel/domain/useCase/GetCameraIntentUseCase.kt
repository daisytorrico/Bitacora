package com.catedra.bitacora.features.travel.domain.useCase

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import com.catedra.bitacora.features.travel.data.local.ImageManager
import javax.inject.Inject

class GetCameraIntentUseCase @Inject constructor(
    private val imageManager: ImageManager
) {
    /**
     * Coordina la creación del archivo temporal y retorna el Intent listo de la cámara
     * junto con la URI generada donde se va a escribir la foto.
     */
    operator fun invoke(): Pair<Intent, Uri> {
        val tempUri = imageManager.createTempPictureUri()
        
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, tempUri)
        }
        
        return Pair(cameraIntent, tempUri)
    }
}
