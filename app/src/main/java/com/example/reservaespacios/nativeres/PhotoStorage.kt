package com.example.reservaespacios.nativeres

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Fotos de los espacios. Se guardan como archivos en el almacenamiento interno de la app
 * (filesDir/space_photos); en SQLite solo se guarda la ruta del archivo.
 * La cámara escribe la foto mediante un FileProvider (no se expone la ruta real a otras apps).
 */
object PhotoStorage {

    private const val DIR = "space_photos"

    fun authority(context: Context) = "${context.packageName}.fileprovider"

    /** Crea un archivo vacío donde la cámara dejará la foto. */
    fun newPhotoFile(context: Context): File {
        val dir = File(context.filesDir, DIR).apply { mkdirs() }
        return File(dir, "space_${System.currentTimeMillis()}.jpg")
    }

    fun uriFor(context: Context, file: File): Uri =
        FileProvider.getUriForFile(context, authority(context), file)

    /** Carga la foto reducida (para no gastar memoria) y corrige la rotación guardada por la cámara. */
    fun loadBitmap(path: String, maxSidePx: Int = 900): Bitmap? {
        val file = File(path)
        if (!file.exists() || file.length() == 0L) return null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxSidePx || bounds.outHeight / (sample * 2) >= maxSidePx) {
            sample *= 2
        }
        val bitmap = BitmapFactory.decodeFile(path, BitmapFactory.Options().apply { inSampleSize = sample })
            ?: return null

        val degrees = try {
            when (ExifInterface(path).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: Exception) {
            0f
        }
        if (degrees == 0f) return bitmap
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    fun delete(path: String?) {
        if (path != null) runCatching { File(path).delete() }
    }
}
