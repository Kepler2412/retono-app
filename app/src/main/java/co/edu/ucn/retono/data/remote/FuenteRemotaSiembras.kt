package co.edu.ucn.retono.data.remote

import co.edu.ucn.retono.domain.model.Lote
import co.edu.ucn.retono.domain.model.Monitoreo
import co.edu.ucn.retono.domain.model.Siembra

/**
 * Partición del lote enviado: qué aceptó el servidor y qué entró en conflicto.
 */
data class ResultadoEnvio(
    val aceptados: List<String>,
    val conflictos: List<String>
)

/**
 * Contrato del receptor remoto.
 *
 * SyncWorker depende de esta interfaz, no de Firestore. Cambiar de backend
 * (a una API REST propia, por ejemplo) implicaría escribir otra implementación
 * sin tocar el motor de sincronización ni el dominio.
 */
interface FuenteRemotaSiembras {

    /** Envía los lotes de terreno. Deben viajar antes que las siembras. */
    suspend fun enviarLotes(lotes: List<Lote>): ResultadoEnvio

    /** Envía un lote de registros y reporta el resultado de cada uno. */
    suspend fun enviarSiembras(siembras: List<Siembra>): ResultadoEnvio

    /** Envía un lote de monitoreos y reporta el resultado de cada uno. */
    suspend fun enviarMonitoreos(monitoreos: List<Monitoreo>): ResultadoEnvio
}
