package co.edu.ucn.retono.data.local

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Custodia de la clave con la que SQLCipher cifra la base de datos.
 *
 * La clave no se codifica en el fuente ni se guarda en SharedPreferences planas.
 * Se genera con SecureRandom en el primer arranque y se persiste dentro de
 * EncryptedSharedPreferences, cuya clave maestra vive en el Android Keystore,
 * respaldado por hardware seguro cuando el dispositivo lo provee.
 *
 * Sin esta medida, en un equipo con acceso root o extraído físicamente el archivo
 * .db sería legible en texto plano con cualquier visor de SQLite. El proyecto
 * maneja coordenadas precisas de predios: eso no es aceptable.
 */
@Singleton
class GestorClaveBd @Inject constructor(
    private val context: Context
) {

    private val preferencias by lazy {
        val claveMaestra = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            ARCHIVO_PREFERENCIAS,
            claveMaestra,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun obtenerClave(): ByteArray {
        preferencias.getString(CLAVE_BD, null)?.let { return it.toByteArray() }

        val nueva = generarClave()
        preferencias.edit().putString(CLAVE_BD, nueva).apply()
        return nueva.toByteArray()
    }

    private fun generarClave(): String {
        val bytes = ByteArray(LONGITUD_CLAVE_BYTES)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val ARCHIVO_PREFERENCIAS = "retono_seguro"
        const val CLAVE_BD = "clave_base_datos"
        const val LONGITUD_CLAVE_BYTES = 32   // AES-256
    }
}
