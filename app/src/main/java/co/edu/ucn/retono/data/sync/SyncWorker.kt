package co.edu.ucn.retono.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.edu.ucn.retono.BuildConfig
import co.edu.ucn.retono.data.auth.RepositorioAutenticacion
import co.edu.ucn.retono.data.remote.FuenteRemotaSiembras
import co.edu.ucn.retono.domain.repository.LoteRepository
import co.edu.ucn.retono.domain.repository.MonitoreoRepository
import co.edu.ucn.retono.domain.repository.SiembraRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.IOException

/**
 * Sube al servidor los registros capturados sin conectividad.
 *
 * WorkManager sostiene la garantía central del proyecto: el trabajo encolado
 * sobrevive al cierre de la aplicación y al reinicio del dispositivo. Un técnico
 * puede registrar cincuenta árboles, quedarse sin batería, cargar el equipo en
 * la noche y encontrar todo sincronizado por la mañana sin haber abierto la app.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted parametros: WorkerParameters,
    private val repository: SiembraRepository,
    private val monitoreoRepository: MonitoreoRepository,
    private val loteRepository: LoteRepository,
    private val fuenteRemota: FuenteRemotaSiembras,
    private val autenticacion: RepositorioAutenticacion
) : CoroutineWorker(context, parametros) {

    override suspend fun doWork(): Result {
        // Sin credenciales de Firebase el proyecto corre en modo local. No tiene
        // sentido reintentar indefinidamente contra un backend inexistente.
        if (!BuildConfig.FIREBASE_HABILITADO) return Result.success()

        // Sin sesión no hay a nombre de quién escribir. Se abandona sin
        // reintentar: el trabajo se reencolará cuando el usuario entre.
        if (!autenticacion.haySesion()) return Result.success()

        val lotesPendientes = loteRepository.obtenerPendientes()
        val siembrasPendientes = repository.obtenerPendientes()
        val monitoreosPendientes = monitoreoRepository.obtenerPendientes()

        if (lotesPendientes.isEmpty() &&
            siembrasPendientes.isEmpty() &&
            monitoreosPendientes.isEmpty()
        ) {
            return Result.success()
        }

        return try {
            // El orden importa y sigue la cadena de dependencias del modelo:
            // lote → siembra → monitoreo. Enviar un monitoreo antes que la
            // siembra que lo origina, o una siembra antes que su lote, dejaría
            // registros huérfanos en el servidor.
            if (lotesPendientes.isNotEmpty()) {
                val resultado = fuenteRemota.enviarLotes(lotesPendientes)
                loteRepository.marcarComoSincronizados(resultado.aceptados)
                loteRepository.marcarEnConflicto(resultado.conflictos)
            }

            if (siembrasPendientes.isNotEmpty()) {
                val resultado = fuenteRemota.enviarSiembras(siembrasPendientes)
                repository.marcarComoSincronizadas(resultado.aceptados)

                // Los conflictos no se descartan ni se sobrescriben en silencio:
                // quedan marcados y visibles en la pantalla de sincronización
                // para que una persona decida.
                repository.marcarEnConflicto(resultado.conflictos)
            }

            if (monitoreosPendientes.isNotEmpty()) {
                val resultado = fuenteRemota.enviarMonitoreos(monitoreosPendientes)
                monitoreoRepository.marcarComoSincronizados(resultado.aceptados)
                monitoreoRepository.marcarEnConflicto(resultado.conflictos)
            }

            // Si quedaron más pendientes de los que cupieron en el lote,
            // se reencola para continuar.
            val quedanPendientes = loteRepository.obtenerPendientes().isNotEmpty() ||
                repository.obtenerPendientes().isNotEmpty() ||
                monitoreoRepository.obtenerPendientes().isNotEmpty()

            if (quedanPendientes) Result.retry() else Result.success()

        } catch (e: IOException) {
            // Fallo de red: transitorio por definición. Reintento exponencial.
            Result.retry()
        } catch (e: Exception) {
            // Firestore lanza sus propias excepciones. Se reintenta igual: el
            // dato local está a salvo y no se pierde nada esperando.
            Result.retry()
        }
    }

    companion object {
        const val NOMBRE_TRABAJO = "sync_siembras"
    }
}
