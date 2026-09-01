package co.edu.ucn.retono.di

import co.edu.ucn.retono.data.remote.FuenteRemotaSiembras
import co.edu.ucn.retono.data.remote.firestore.FirestoreFuenteRemota
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ModuloFirebase {

    /*
     * Se usan las instancias estándar y no las extensiones .ktx: esos artefactos
     * quedaron obsoletos al integrarse las APIs de Kotlin en los módulos
     * principales, y depender de ellos ata el proyecto a versiones antiguas
     * del BOM.
     */

    @Provides
    @Singleton
    fun proveerAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * No se configuran ajustes de caché explícitamente: en Android la
     * persistencia de Firestore viene activa por defecto, y las APIs para
     * modificarla han cambiado varias veces entre versiones. Además, la caché de
     * Firestore no es la fuente de verdad del proyecto —Room lo es—, así que su
     * configuración fina no es relevante aquí. Ver ADR-007.
     */
    @Provides
    @Singleton
    fun proveerFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class ModuloRemoto {

    @Binds
    @Singleton
    abstract fun vincularFuenteRemota(impl: FirestoreFuenteRemota): FuenteRemotaSiembras
}
