package co.edu.ucn.retono

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Punto de entrada de la aplicación.
 *
 * Implementa Configuration.Provider para que WorkManager use la fábrica de Hilt
 * y pueda inyectar dependencias en SyncWorker. Sin esto, el worker no podría
 * recibir el repositorio ni la API.
 */
@HiltAndroidApp
class RetonoApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
