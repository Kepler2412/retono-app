package co.edu.ucn.retono.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import co.edu.ucn.retono.BuildConfig
import co.edu.ucn.retono.data.local.DatosIniciales
import co.edu.ucn.retono.data.local.GestorClaveBd
import co.edu.ucn.retono.data.local.RetonoDatabase
import co.edu.ucn.retono.data.local.dao.EspecieDao
import co.edu.ucn.retono.data.local.dao.LoteDao
import co.edu.ucn.retono.data.local.dao.MonitoreoDao
import co.edu.ucn.retono.data.local.dao.SiembraDao
import co.edu.ucn.retono.data.local.dao.ViveroDao
import co.edu.ucn.retono.data.repository.EspecieRepositoryImpl
import co.edu.ucn.retono.data.repository.LoteRepositoryImpl
import co.edu.ucn.retono.data.repository.MonitoreoRepositoryImpl
import co.edu.ucn.retono.data.repository.SiembraRepositoryImpl
import co.edu.ucn.retono.domain.repository.EspecieRepository
import co.edu.ucn.retono.domain.repository.LoteRepository
import co.edu.ucn.retono.domain.repository.MonitoreoRepository
import co.edu.ucn.retono.domain.repository.SiembraRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModuloApp {

    /**
     * Hilt sabe proveer el Context de aplicación mediante @ApplicationContext,
     * pero las clases que lo reciben por constructor sin esa anotación
     * (GestorClaveBd, PlanificadorSync, ProveedorUbicacion) necesitan este
     * puente explícito.
     */
    @Provides
    @Singleton
    fun proveerContexto(@ApplicationContext context: Context): Context = context
}

@Module
@InstallIn(SingletonComponent::class)
object ModuloDatos {

    @Provides
    @Singleton
    fun proveerBaseDatos(
        @ApplicationContext context: Context,
        gestorClave: GestorClaveBd
    ): RetonoDatabase {
        // SupportFactory conecta Room con SQLCipher: el archivo .db queda
        // cifrado con AES-256 en reposo.
        val factory = SupportFactory(gestorClave.obtenerClave())

        return Room.databaseBuilder(
            context,
            RetonoDatabase::class.java,
            RetonoDatabase.NOMBRE
        )
            .openHelperFactory(factory)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // El catálogo de especies debe existir antes de la primera
                    // salida a campo: allí no habrá red para descargarlo.
                    DatosIniciales.sembrar(db)
                }
            })
            // Sin fallbackToDestructiveMigration: borrar los datos del técnico
            // ante un cambio de esquema sería inaceptable. Las migraciones se
            // escriben explícitamente.
            .build()
    }

    @Provides fun proveerLoteDao(bd: RetonoDatabase): LoteDao = bd.loteDao()
    @Provides fun proveerSiembraDao(bd: RetonoDatabase): SiembraDao = bd.siembraDao()
    @Provides fun proveerMonitoreoDao(bd: RetonoDatabase): MonitoreoDao = bd.monitoreoDao()
    @Provides fun proveerEspecieDao(bd: RetonoDatabase): EspecieDao = bd.especieDao()
    @Provides fun proveerViveroDao(bd: RetonoDatabase): ViveroDao = bd.viveroDao()

}

@Module
@InstallIn(SingletonComponent::class)
abstract class ModuloRepositorios {

    @Binds
    @Singleton
    abstract fun vincularSiembraRepository(impl: SiembraRepositoryImpl): SiembraRepository

    @Binds
    @Singleton
    abstract fun vincularLoteRepository(impl: LoteRepositoryImpl): LoteRepository

    @Binds
    @Singleton
    abstract fun vincularMonitoreoRepository(impl: MonitoreoRepositoryImpl): MonitoreoRepository

    @Binds
    @Singleton
    abstract fun vincularEspecieRepository(impl: EspecieRepositoryImpl): EspecieRepository
}
