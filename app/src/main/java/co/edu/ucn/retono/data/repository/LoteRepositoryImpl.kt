package co.edu.ucn.retono.data.repository

import co.edu.ucn.retono.data.local.dao.LoteDao
import co.edu.ucn.retono.data.sync.PlanificadorSync
import co.edu.ucn.retono.domain.model.Lote
import co.edu.ucn.retono.domain.repository.LoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoteRepositoryImpl @Inject constructor(
    private val dao: LoteDao,
    private val planificador: PlanificadorSync
) : LoteRepository {

    override fun observarLotes(viveroId: String): Flow<List<Lote>> =
        dao.observarPorVivero(viveroId).map { lista -> lista.map { it.aDominio() } }

    override fun observarLote(id: String): Flow<Lote?> =
        dao.observarPorId(id).map { it?.aDominio() }

    override suspend fun guardarLote(lote: Lote) {
        dao.insertar(lote.aEntidad())
        planificador.encolarSincronizacion()
    }

    override suspend fun obtenerPendientes(): List<Lote> =
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
