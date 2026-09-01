package co.edu.ucn.retono.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Captura y procesamiento de las fotografías de campo.
 *
 * El procesamiento no es cosmético, responde a tres restricciones del proyecto:
 *
 * 1. **Tamaño.** Una foto de un móvil actual ronda los 4 MB. Una jornada con
 *    cincuenta árboles produciría 200 MB que habría que subir por una conexión
 *    rural. Reescalar y recomprimir baja eso en más de un orden de magnitud.
 *
 * 2. **Privacidad.** Los archivos JPEG guardan metadatos EXIF que incluyen
 *    coordenadas GPS, modelo del equipo y número de serie. El proyecto ya
 *    almacena la coordenada de forma explícita y controlada; duplicarla oculta
 *    dentro de la imagen es exponer información sin que el usuario lo sepa.
 *
 * 3. **Orientación.** Al eliminar el EXIF se pierde también la etiqueta de
 *    rotación, así que hay que aplicarla a los píxeles antes de descartarla.
 *    Omitir este paso deja todas las fotos de lado.
 */
@Singleton
class GestorFotografias @Inject constructor(
    private val context: Context
) {

    /** Carpeta privada de la aplicación. Otras apps no pueden leerla. */
    private val carpetaFotos: File
        get() = File(context.filesDir, "fotos").apply { mkdirs() }

    /**
     * Crea el archivo destino y devuelve el Uri que se entrega a la cámara.
     * Se usa FileProvider porque desde Android 7 compartir un file:// directo
     * lanza FileUriExposedException.
     */
    fun prepararDestino(): Pair<File, Uri> {
        val archivo = File(carpetaFotos, "tmp_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, AUTORIDAD, archivo)
        return archivo to uri
    }

    /**
     * Reescala, corrige orientación, recomprime y elimina los metadatos.
     * Devuelve la ruta del archivo final, o el error si la imagen no se pudo leer.
     */
    suspend fun procesar(original: File): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val bitmapOriginal = BitmapFactory.decodeFile(original.absolutePath)
                ?: throw IllegalStateException("No se pudo leer la fotografía capturada")

            val rotacion = leerRotacion(original)
            val redimensionado = redimensionar(bitmapOriginal)
            val orientado = aplicarRotacion(redimensionado, rotacion)

            val destino = File(carpetaFotos, "siembra_${UUID.randomUUID()}.jpg")
            FileOutputStream(destino).use { salida ->
                // Recomprimir desde el bitmap descarta todo el EXIF de origen:
                // el archivo resultante no lleva GPS ni identificadores del equipo.
                orientado.compress(Bitmap.CompressFormat.JPEG, CALIDAD_JPEG, salida)
            }

            bitmapOriginal.recycle()
            if (orientado != bitmapOriginal) orientado.recycle()
            original.delete()

            destino.absolutePath
        }
    }

    private fun leerRotacion(archivo: File): Float =
        when (
            ExifInterface(archivo.absolutePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        ) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }

    private fun redimensionar(origen: Bitmap): Bitmap {
        val mayor = maxOf(origen.width, origen.height)
        if (mayor <= LADO_MAXIMO_PX) return origen

        val factor = LADO_MAXIMO_PX.toFloat() / mayor
        return Bitmap.createScaledBitmap(
            origen,
            (origen.width * factor).toInt(),
            (origen.height * factor).toInt(),
            true
        )
    }

    private fun aplicarRotacion(origen: Bitmap, grados: Float): Bitmap {
        if (grados == 0f) return origen
        val matriz = Matrix().apply { postRotate(grados) }
        return Bitmap.createBitmap(origen, 0, 0, origen.width, origen.height, matriz, true)
    }

    private companion object {
        const val AUTORIDAD = "co.edu.ucn.retono.fileprovider"

        /**
         * 1600 px en el lado mayor. Suficiente para identificar la especie y el
         * estado del individuo, que es para lo que sirve la foto; más resolución
         * solo añadiría peso de subida sin valor documental.
         */
        const val LADO_MAXIMO_PX = 1600
        const val CALIDAD_JPEG = 80
    }
}
