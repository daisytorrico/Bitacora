package com.catedra.bitacora.features.travel.domain.useCase

import android.content.Intent
import javax.inject.Inject

class GetGalleryIntentUseCase @Inject constructor() {
    
    operator fun invoke(allowMultiple: Boolean = false): Intent {
        return Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            if (allowMultiple) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
    }
}
