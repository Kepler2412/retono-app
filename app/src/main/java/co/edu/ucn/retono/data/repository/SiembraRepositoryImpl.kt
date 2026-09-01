package co.edu.ucn.retono.data.repository

import co.edu.ucn.retono.data.local.dao.SiembraDao
import co.edu.ucn.retono.data.sync.PlanificadorSync
import co.edu.ucn.retono.domain.model.Siembra
import co.edu.ucn.retono.domain.repository.SiembraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación offline-first.
 *
 * El principio rector: la base de datos local es la única fuente de verdad.
 * Este repositorio nunca expone la red hacia arriba. Escribe en Room, encola el
 * envío y retorna. La UI recibe la confirmación de inmediato y se comporta igual
 * con y sin señal.
 */
@Singleton
class SiembraRepositoryImpl @Inject constructor(
    private val dao: SiembraDao,
    private val planificador: PlanificadorSync
) : SiembraRepository {

    override fun observarSiembrasDeLote(loteId: String): Flow<List<Siembra>> =
        dao.observarPorLote(loteId).map { lista -> lista.map { it.aDominio() } }

    override fun observarPendientesDeSync(): Flow<List<Siembra>> =
        dao.observarPendientes().map { lista -> lista.map { it.aDominio() } }

    override suspend fun registrarSiembra(siembra: Siembra) {
        // 1. Escritura local. Es la operación que realmente importa: si esto
        //    tiene éxito, el dato del técnico ya está a salvo.
        dao.insertar(siembra.aEntidad())

        // 2. Encolar el envío. WorkManager decide cuándo ejecutarlo según la
        //    conectividad; no bloqueamos al usuario esperando red.
        planificador.encolarSincronizacion()
    }

    override suspend fun obtenerPendientes(): List<Siembra> =
        dao.obtenerPendientes(TAMANO_LOTE_SYNC).map { it.aDominio() }

    override suspend fun marcarComoSincronizadas(ids: List<String>) {
        if (ids.isNotEmpty()) dao.marcarSincronizadas(ids)
    }

    override suspend fun marcarEnConflicto(ids: List<String>) {
        if (ids.isNotEmpty()) dao.actualizarEstado(ids, "CONFLICTO")
    }

    private companion object {
        /**
         * Cuántos registros viajan en cada envío. Un lote muy grande agotaría el
         * tiempo de espera en una conexión rural débil; uno muy pequeño
         * multiplicaría las peticiones. Si quedan pendientes tras el envío,
         * SyncWorker se reencola y continúa.
         */
        const val TAMANO_LOTE_SYNC = 100
    }
}
