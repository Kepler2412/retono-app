package co.edu.ucn.retono.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import co.edu.ucn.retono.data.local.entity.LoteEntity
import co.edu.ucn.retono.data.local.entity.MonitoreoEntity
import co.edu.ucn.retono.data.local.entity.SiembraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoteDao {

    @Query("SELECT * FROM lotes WHERE viveroId = :viveroId ORDER BY nombre")
    fun observarPorVivero(viveroId: String): Flow<List<LoteEntity>>

    @Query("SELECT * FROM lotes WHERE id = :id")
    fun observarPorId(id: String): Flow<LoteEntity?>

    @Query("SELECT * FROM lotes WHERE estadoSync = 'PENDIENTE' ORDER BY actualizadoEn LIMIT :limite")
    suspend fun obtenerPendientes(limite: Int): List<LoteEntity>

    @Query("SELECT COUNT(*) FROM lotes")
    suspend fun contar(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(lote: LoteEntity)

    @Query("UPDATE lotes SET estadoSync = :estado WHERE id IN (:ids)")
    suspend fun actualizarEstado(ids: List<String>, estado: String)
}

@Dao
interface SiembraDao {

    /**
     * Devuelve un Flow: Room reemite automáticamente cuando la tabla cambia.
     * Esto es lo que permite que la UI se actualice sola tras una sincronización,
     * sin que el ViewModel tenga que enterarse de que hubo red.
     */
    @Query("SELECT * FROM siembras WHERE loteId = :loteId ORDER BY fechaSiembra DESC")
    fun observarPorLote(loteId: String): Flow<List<SiembraEntity>>

    @Query("SELECT * FROM siembras WHERE estadoSync = 'PENDIENTE' ORDER BY actualizadoEn")
    fun observarPendientes(): Flow<List<SiembraEntity>>

    @Query("SELECT * FROM siembras WHERE estadoSync = 'PENDIENTE' ORDER BY actualizadoEn LIMIT :limite")
    suspend fun obtenerPendientes(limite: Int): List<SiembraEntity>

    @Query("SELECT COUNT(*) FROM siembras WHERE estadoSync = 'PENDIENTE'")
    fun contarPendientes(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(siembra: SiembraEntity)

    @Query("UPDATE siembras SET estadoSync = :estado WHERE id IN (:ids)")
    suspend fun actualizarEstado(ids: List<String>, estado: String)

    /**
     * Marca un lote de registros como sincronizados en una sola transacción.
     * Si el proceso muere a mitad de camino, ningún registro queda en un estado
     * intermedio inconsistente.
     */
    @Transaction
    suspend fun marcarSincronizadas(ids: List<String>) {
        actualizarEstado(ids, "SINCRONIZADO")
    }
}

@Dao
interface MonitoreoDao {

    @Query(
        """
        SELECT m.* FROM monitoreos m
        INNER JOIN siembras s ON s.id = m.siembraId
        WHERE s.loteId = :loteId
        ORDER BY m.fecha DESC
        """
    )
    fun observarPorLote(loteId: String): Flow<List<MonitoreoEntity>>

    @Query("SELECT * FROM monitoreos WHERE estadoSync = 'PENDIENTE' ORDER BY actualizadoEn LIMIT :limite")
    suspend fun obtenerPendientes(limite: Int): List<MonitoreoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(monitoreo: MonitoreoEntity)

    @Query("UPDATE monitoreos SET estadoSync = :estado WHERE id IN (:ids)")
    suspend fun actualizarEstado(ids: List<String>, estado: String)
}
