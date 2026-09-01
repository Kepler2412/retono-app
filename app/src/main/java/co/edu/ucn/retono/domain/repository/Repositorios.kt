package co.edu.ucn.retono.domain.repository

import co.edu.ucn.retono.domain.model.Especie
import co.edu.ucn.retono.domain.model.Lote
import co.edu.ucn.retono.domain.model.Monitoreo
import co.edu.ucn.retono.domain.model.Siembra
import kotlinx.coroutines.flow.Flow

/**
 * Contratos que la capa de datos debe cumplir.
 *
 * Se declaran en el dominio, no en datos: así el dominio no depende de la
 * implementación, sino la implementación del dominio (inversión de dependencias).
 */
interface LoteRepository {
    fun observarLotes(viveroId: String): Flow<List<Lote>>
    fun observarLote(id: String): Flow<Lote?>

    /** Crea o actualiza. El identificador lo decide quien llama. */
    suspend fun guardarLote(lote: Lote)

    suspend fun obtenerPendientes(): List<Lote>
    suspend fun marcarComoSincronizados(ids: List<String>)
    suspend fun marcarEnConflicto(ids: List<String>)
}

interface SiembraRepository {
    /** Emite desde la base local. La UI nunca consulta la red directamente. */
    fun observarSiembrasDeLote(loteId: String): Flow<List<Siembra>>

    fun observarPendientesDeSync(): Flow<List<Siembra>>

    /**
     * Persiste localmente y encola la sincronización.
     * Retorna cuando el dato ya está en disco, sin esperar a la red.
     */
    suspend fun registrarSiembra(siembra: Siembra)

    suspend fun obtenerPendientes(): List<Siembra>
    suspend fun marcarComoSincronizadas(ids: List<String>)
    suspend fun marcarEnConflicto(ids: List<String>)
}

interface EspecieRepository {
    /** Catálogo de especies nativas. Se precarga localmente: en campo no hay red. */
    fun observarEspecies(): Flow<List<Especie>>
}

interface MonitoreoRepository {
    fun observarMonitoreosDeLote(loteId: String): Flow<List<Monitoreo>>
    suspend fun registrarMonitoreo(monitoreo: Monitoreo)

    suspend fun obtenerPendientes(): List<Monitoreo>
    suspend fun marcarComoSincronizados(ids: List<String>)
    suspend fun marcarEnConflicto(ids: List<String>)
}
