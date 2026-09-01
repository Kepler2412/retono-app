package co.edu.ucn.retono.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encola la sincronización con las restricciones adecuadas.
 */
@Singleton
class PlanificadorSync @Inject constructor(
    private val context: Context
) {

    fun encolarSincronizacion() {
        val restricciones = Constraints.Builder()
            // El trabajo solo se ejecuta cuando hay red. El sistema operativo
            // se encarga de despertarlo: la aplicación no sondea la conectividad
            // ni mantiene un servicio activo, lo que preserva batería durante
            // jornadas largas de campo.
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val solicitud = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(restricciones)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                RETARDO_INICIAL_SEGUNDOS,
                TimeUnit.SECONDS
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncWorker.NOMBRE_TRABAJO,
            // KEEP y no REPLACE: registrar veinte siembras seguidas no debe
            // cancelar y recrear el trabajo veinte veces. Un solo trabajo
            // pendiente recoge todo lo acumulado cuando llegue la red.
            ExistingWorkPolicy.KEEP,
            solicitud
        )
    }

    private companion object {
        const val RETARDO_INICIAL_SEGUNDOS = 30L
    }
}
