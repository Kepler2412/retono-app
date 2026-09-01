package co.edu.ucn.retono.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import co.edu.ucn.retono.data.local.entity.EspecieEntity
import co.edu.ucn.retono.data.local.entity.ViveroEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EspecieDao {

    @Query("SELECT * FROM especies ORDER BY nombreComun")
    fun observarTodas(): Flow<List<EspecieEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodas(especies: List<EspecieEntity>)
}

@Dao
interface ViveroDao {

    @Query("SELECT * FROM viveros ORDER BY nombre")
    fun observarTodos(): Flow<List<ViveroEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(vivero: ViveroEntity)
}
