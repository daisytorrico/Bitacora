package com.catedra.bitacora.features.travel.domain.useCase

import android.content.Intent
import javax.inject.Inject

class GetGalleryIntentUseCase @Inject constructor() {
    
    operator fun invoke(): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
    }
}
