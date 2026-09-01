package co.edu.ucn.retono.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/** Lectura de GPS con su precisión asociada. */
data class Ubicacion(
    val latitud: Double,
    val longitud: Double,
    val precisionMetros: Float
)

/**
 * Obtiene una lectura puntual de GPS de alta precisión.
 *
 * Se usa `getCurrentLocation` y no `lastLocation`: la última posición conocida
 * puede tener horas de antigüedad y corresponder a otro lote. Para georreferenciar
 * un árbol concreto se necesita una lectura tomada en ese momento, aunque cueste
 * unos segundos de espera.
 *
 * La solicitud es puntual y no continua: no se mantiene el GPS activo, lo que
 * preserva batería durante jornadas largas de campo.
 */
@Singleton
class ProveedorUbicacion @Inject constructor(
    private val context: Context
) {

    private val cliente by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun tienePermiso(): Boolean =
        ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")   // el llamador verifica con tienePermiso()
    suspend fun obtenerUbicacion(): Result<Ubicacion> {
        if (!tienePermiso()) {
            return Result.failure(SecurityException("Permiso de ubicación no concedido"))
        }

        return suspendCancellableCoroutine { continuacion ->
            val cancelacion = CancellationTokenSource()

            cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancelacion.token)
                .addOnSuccessListener { location ->
                    if (location == null) {
                        continuacion.resume(
                            Result.failure(
                                IllegalStateException(
                                    "No se obtuvo señal de GPS. Salga a cielo abierto e intente de nuevo."
                                )
                            )
                        )
                    } else {
                        continuacion.resume(
                            Result.success(
                                Ubicacion(
                                    latitud = location.latitude,
                                    longitud = location.longitude,
                                    precisionMetros = location.accuracy
                                )
                            )
                        )
                    }
                }
                .addOnFailureListener { continuacion.resume(Result.failure(it)) }

            // Si el usuario abandona la pantalla, se cancela la petición al GPS
            // en lugar de dejarla consumiendo energía.
            continuacion.invokeOnCancellation { cancelacion.cancel() }
        }
    }
}
