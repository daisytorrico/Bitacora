package com.catedra.bitacora.features.travel.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import com.catedra.bitacora.features.travel.domain.repository.ImageRepository

@Singleton
class ImageManager @Inject constructor(
    @ApplicationContext private val context: Context
) : ImageRepository {
    /**
     * Crea un archivo temporal en el caché y retorna su URI.
     * Se usa principalmente para capturar fotos con la cámara.
     */
    override fun createTempPictureUri(): Uri {
        val imagesDir = File(context.externalCacheDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }

        val tempFile = File.createTempFile(
            "IMG_${System.currentTimeMillis()}_",
            ".jpg",
            imagesDir
        ).apply {
            createNewFile()
            deleteOnExit()
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    /**
     * Toma una URI de imagen, la comprime y devuelve la URI del nuevo archivo optimizado.
     * Se ejecuta en un hilo secundario (IO) para no bloquear la UI.
     */
    override suspend fun compressImage(uri: Uri): Uri? = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) return@withContext null

            // Crear archivo para la imagen comprimida
            val compressedFile = File(context.externalCacheDir, "images/COMPRESSED_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(compressedFile)

            // Comprimir: 80% de calidad
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            
            outputStream.flush()
            outputStream.close()

            // Liberar memoria del bitmap
            bitmap.recycle()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                compressedFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
