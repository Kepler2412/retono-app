package co.edu.ucn.retono.data.auth

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Usuario de la sesión, reducido a lo que la aplicación necesita. */
data class UsuarioSesion(
    val uid: String,
    val correo: String?
)

/**
 * Autenticación con Firebase.
 *
 * Punto clave para el comportamiento offline: Firebase persiste la sesión en el
 * dispositivo tras el primer inicio exitoso. El técnico se autentica una vez
 * con conectividad y luego trabaja días en campo sin red; la sesión sigue
 * siendo válida y los registros conservan su autoría.
 *
 * El corolario es que **iniciar sesión sí requiere red**. Por eso el login no se
 * exige en cada apertura, solo la primera vez.
 */
@Singleton
class RepositorioAutenticacion @Inject constructor(
    private val auth: FirebaseAuth
) {

    /** Emite el usuario actual y cada cambio posterior de sesión. */
    val sesion: Flow<UsuarioSesion?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { instancia ->
            trySend(instancia.currentUser?.let { UsuarioSesion(it.uid, it.email) })
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    fun uidActual(): String? = auth.currentUser?.uid

    fun haySesion(): Boolean = auth.currentUser != null

    suspend fun iniciarSesion(correo: String, contrasena: String): Result<Unit> =
        runCatching {
            auth.signInWithEmailAndPassword(correo.trim(), contrasena).await()
            Unit
        }

    suspend fun registrar(correo: String, contrasena: String): Result<Unit> =
        runCatching {
            auth.createUserWithEmailAndPassword(correo.trim(), contrasena).await()
            Unit
        }

    /**
     * Cerrar sesión borra la credencial, pero **no** los datos locales.
     * Un registro pendiente de sincronizar no puede desaparecer porque alguien
     * salió de la cuenta: sería perder trabajo de campo ya hecho.
     */
    fun cerrarSesion() = auth.signOut()
}
