package co.edu.ucn.retono.data.repository

import co.edu.ucn.retono.data.local.dao.MonitoreoDao
import co.edu.ucn.retono.data.sync.PlanificadorSync
import co.edu.ucn.retono.domain.model.Monitoreo
import co.edu.ucn.retono.domain.repository.MonitoreoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitoreoRepositoryImpl @Inject constructor(
    private val dao: MonitoreoDao,
    private val planificador: PlanificadorSync
) : MonitoreoRepository {

    override fun observarMonitoreosDeLote(loteId: String): Flow<List<Monitoreo>> =
        dao.observarPorLote(loteId).map { lista -> lista.map { it.aDominio() } }

    override suspend fun registrarMonitoreo(monitoreo: Monitoreo) {
        dao.insertar(monitoreo.aEntidad())
        planificador.encolarSincronizacion()
    }

    override suspend fun obtenerPendientes(): List<Monitoreo> =
        dao.obtenerPendientes(TAMANO_LOTE_SYNC).map { it.aDominio() }

    override suspend fun marcarComoSincronizados(ids: List<String>) {
        if (ids.isNotEmpty()) dao.actualizarEstado(ids, "SINCRONIZADO")
    }

    override suspend fun marcarEnConflicto(ids: List<String>) {
        if (ids.isNotEmpty()) dao.actualizarEstado(ids, "CONFLICTO")
    }

    private companion object {
        const val TAMANO_LOTE_SYNC = 100
    }
}
