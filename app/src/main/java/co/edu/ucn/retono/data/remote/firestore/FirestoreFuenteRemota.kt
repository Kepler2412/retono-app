package co.edu.ucn.retono.data.remote.firestore

import co.edu.ucn.retono.data.auth.RepositorioAutenticacion
import co.edu.ucn.retono.data.remote.FuenteRemotaSiembras
import co.edu.ucn.retono.data.remote.ResultadoEnvio
import co.edu.ucn.retono.domain.model.Lote
import co.edu.ucn.retono.domain.model.Monitoreo
import co.edu.ucn.retono.domain.model.Siembra
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del receptor remoto sobre Cloud Firestore.
 *
 * Sobre la resolución de conflictos: cada envío se ejecuta dentro de una
 * transacción que compara la marca temporal local contra la remota. Si el
 * servidor tiene una versión más reciente, el registro no se sobrescribe: se
 * reporta como conflicto para que una persona decida. Esto cuesta una lectura
 * por documento, precio razonable en un dominio donde los datos sustentan
 * verificación ante entidades financiadoras.
 *
 * Nota importante: Firestore trae su propia caché offline, pero **no** se usa
 * como fuente de verdad. Room lo es. La razón es que la caché de Firestore es
 * un detalle de su SDK, opaca y sin control sobre el estado por registro; el
 * proyecto necesita saber exactamente qué está pendiente y poder mostrarlo.
 * Ver ADR-007.
 */
@Singleton
class FirestoreFuenteRemota @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val autenticacion: RepositorioAutenticacion
) : FuenteRemotaSiembras {

    override suspend fun enviarLotes(lotes: List<Lote>): ResultadoEnvio =
        enviarLote(
            coleccion = COLECCION_LOTES,
            registros = lotes,
            id = { it.id },
            actualizadoEn = { it.actualizadoEn },
            aDocumento = ::loteAMapa
        )

    override suspend fun enviarSiembras(siembras: List<Siembra>): ResultadoEnvio =
        enviarLote(
            coleccion = COLECCION_SIEMBRAS,
            registros = siembras,
            id = { it.id },
            actualizadoEn = { it.actualizadoEn },
            aDocumento = ::siembraAMapa
        )

    override suspend fun enviarMonitoreos(monitoreos: List<Monitoreo>): ResultadoEnvio =
        enviarLote(
            coleccion = COLECCION_MONITOREOS,
            registros = monitoreos,
            id = { it.id },
            actualizadoEn = { it.actualizadoEn },
            aDocumento = ::monitoreoAMapa
        )

    /**
     * Envío genérico con detección de conflicto por marca temporal.
     *
     * La lógica es idéntica para siembras y monitoreos, así que se extrae en vez
     * de duplicarla: un cambio en la política de conflictos debe ocurrir en un
     * solo lugar.
     */
    private suspend fun <T> enviarLote(
        coleccion: String,
        registros: List<T>,
        id: (T) -> String,
        actualizadoEn: (T) -> Long,
        aDocumento: (T, String) -> Map<String, Any?>
    ): ResultadoEnvio {
        val uid = autenticacion.uidActual()
            ?: throw IllegalStateException("No hay sesión activa: no se puede sincronizar")

        val aceptados = mutableListOf<String>()
        val conflictos = mutableListOf<String>()

        registros.forEach { registro ->
            val documento = firestore.collection(coleccion).document(id(registro))

            val huboConflicto = firestore.runTransaction { transaccion ->
                val remoto = transaccion.get(documento)

                if (remoto.exists()) {
                    val actualizadoRemoto = remoto.getLong(CAMPO_ACTUALIZADO) ?: 0L
                    if (actualizadoRemoto > actualizadoEn(registro)) {
                        // El servidor va por delante. No se pisa el dato ajeno.
                        return@runTransaction true
                    }
                }

                transaccion.set(documento, aDocumento(registro, uid))
                false
            }.await()

            if (huboConflicto) conflictos += id(registro) else aceptados += id(registro)
        }

        return ResultadoEnvio(aceptados = aceptados, conflictos = conflictos)
    }

    private fun siembraAMapa(siembra: Siembra, uid: String): Map<String, Any?> = mapOf(
        "id" to siembra.id,
        "loteId" to siembra.loteId,
        "especieId" to siembra.especieId,
        "latitud" to siembra.latitud,
        "longitud" to siembra.longitud,
        "precisionMetros" to siembra.precisionMetros.toDouble(),
        "fechaSiembra" to siembra.fechaSiembra,
        "observaciones" to siembra.observaciones,
        CAMPO_ACTUALIZADO to siembra.actualizadoEn,
        // Se guarda quién registró: las reglas de Firestore lo exigen y permite
        // trazar la autoría de cada dato de campo.
        "registradoPor" to uid
    )

    private fun loteAMapa(lote: Lote, uid: String): Map<String, Any?> = mapOf(
        "id" to lote.id,
        "viveroId" to lote.viveroId,
        "nombre" to lote.nombre,
        "areaHectareas" to lote.areaHectareas,
        "latitudCentroide" to lote.latitudCentroide,
        "longitudCentroide" to lote.longitudCentroide,
        CAMPO_ACTUALIZADO to lote.actualizadoEn,
        "registradoPor" to uid
    )

    private fun monitoreoAMapa(monitoreo: Monitoreo, uid: String): Map<String, Any?> = mapOf(
        "id" to monitoreo.id,
        "siembraId" to monitoreo.siembraId,
        "fecha" to monitoreo.fecha,
        "estadoVital" to monitoreo.estadoVital.name,
        "alturaCm" to monitoreo.alturaCm,
        "diametroMm" to monitoreo.diametroMm,
        "observaciones" to monitoreo.observaciones,
        CAMPO_ACTUALIZADO to monitoreo.actualizadoEn,
        "registradoPor" to uid
    )

    private companion object {
        const val COLECCION_LOTES = "lotes"
        const val COLECCION_SIEMBRAS = "siembras"
        const val COLECCION_MONITOREOS = "monitoreos"
        const val CAMPO_ACTUALIZADO = "actualizadoEn"
    }
}
