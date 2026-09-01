package co.edu.ucn.retono.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import co.edu.ucn.retono.data.local.dao.EspecieDao
import co.edu.ucn.retono.data.local.dao.LoteDao
import co.edu.ucn.retono.data.local.dao.MonitoreoDao
import co.edu.ucn.retono.data.local.dao.SiembraDao
import co.edu.ucn.retono.data.local.dao.ViveroDao
import co.edu.ucn.retono.data.local.entity.EspecieEntity
import co.edu.ucn.retono.data.local.entity.LoteEntity
import co.edu.ucn.retono.data.local.entity.MonitoreoEntity
import co.edu.ucn.retono.data.local.entity.SiembraEntity
import co.edu.ucn.retono.data.local.entity.ViveroEntity

@Database(
    entities = [
        ViveroEntity::class,
        LoteEntity::class,
        EspecieEntity::class,
        SiembraEntity::class,
        MonitoreoEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class RetonoDatabase : RoomDatabase() {
    abstract fun loteDao(): LoteDao
    abstract fun siembraDao(): SiembraDao
    abstract fun monitoreoDao(): MonitoreoDao
    abstract fun especieDao(): EspecieDao
    abstract fun viveroDao(): ViveroDao

    companion object {
        const val NOMBRE = "retono.db"
    }
}
