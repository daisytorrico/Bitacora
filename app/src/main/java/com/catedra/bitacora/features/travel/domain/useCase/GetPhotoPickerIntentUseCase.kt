package com.catedra.bitacora.features.travel.domain.useCase

import android.content.Intent
import android.net.Uri
import javax.inject.Inject

class GetPhotoPickerIntentUseCase @Inject constructor(
    private val getCameraIntentUseCase: GetCameraIntentUseCase,
    private val getGalleryIntentUseCase: GetGalleryIntentUseCase
) {
    /**
     * Crea un Intent de tipo Chooser que permite al usuario elegir entre 
     * usar la cámara o seleccionar una imagen de la galería.
     * @return Un Pair con el Intent del Chooser y la URI temporal donde se guardará la foto de la cámara.
     */
    operator fun invoke(
        title: String = "Seleccionar Foto",
        allowMultiple: Boolean = false
    ): Pair<Intent, Uri> {
        val (cameraIntent, tempUri) = getCameraIntentUseCase()
        val galleryIntent = getGalleryIntentUseCase(allowMultiple)
        
        val chooserIntent = Intent.createChooser(galleryIntent, title).apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        }
        
        return Pair(chooserIntent, tempUri)
    }
}
