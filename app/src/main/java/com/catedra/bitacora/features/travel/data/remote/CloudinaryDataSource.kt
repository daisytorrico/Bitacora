package com.catedra.bitacora.features.travel.data.remote

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudinaryDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Valores de la consola de Cloudinary
    private val cloudName = "dx3tqpadc" 
    private val uploadPreset = "bitacora_preset"

    init {
        try {
            val config = mapOf(
                "cloud_name" to cloudName,
                "secure" to true
            )
            MediaManager.init(context, config)
        } catch (e: Exception) {
            // Ya inicializado o error
        }
    }

    suspend fun uploadImage(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .option("unsigned", true)
            .option("upload_preset", uploadPreset)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url") as? String
                    if (url != null) {
                        continuation.resume(url)
                    } else {
                        continuation.resumeWithException(Exception("Error al obtener URL de Cloudinary"))
                    }
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    continuation.resumeWithException(Exception(error?.description ?: "Error en subida"))
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            })
            .dispatch()
    }
}
