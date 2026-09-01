package co.edu.ucn.retono.domain.usecase

import co.edu.ucn.retono.domain.model.EstadoSync
import co.edu.ucn.retono.domain.model.Lote
import co.edu.ucn.retono.domain.repository.LoteRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Crea un lote nuevo o actualiza uno existente.
 *
 * Un lote es el contenedor de todas las siembras de una intervención, y su área
 * entra en el denominador de indicadores como la densidad de siembra. Un dato
 * mal capturado aquí contamina todo lo que cuelgue de él, de ahí que las
 * validaciones sean estrictas.
 */
class GuardarLote @Inject constructor(
    private val repository: LoteRepository
) {

    suspend operator fun invoke(parametros: Parametros): Result<String> {
        val nombre = parametros.nombre.trim()

        if (nombre.length < LONGITUD_MINIMA_NOMBRE) {
            return Result.failure(
                IllegalArgumentException(
                    "El nombre del lote debe tener al menos $LONGITUD_MINIMA_NOMBRE caracteres"
                )
            )
        }

        if (parametros.areaHectareas <= 0) {
            return Result.failure(
                IllegalArgumentException("El área debe ser mayor que cero")
            )
        }

        if (parametros.areaHectareas > AREA_MAXIMA_HECTAREAS) {
            return Result.failure(
                IllegalArgumentException(
                    "Un área de ${parametros.areaHectareas} ha es inusual para un lote de " +
                        "vivero comunitario. Verifique si son hectáreas y no metros cuadrados."
                )
            )
        }

        if (parametros.latitud !in -90.0..90.0 || parametros.longitud !in -180.0..180.0) {
            return Result.failure(
                IllegalArgumentException("Las coordenadas del centroide no son válidas")
            )
        }

        // Si viene identificador es una edición; si no, se crea uno nuevo.
        // El mismo caso de uso sirve para ambos porque la operación de fondo es
        // idéntica: persistir el lote y encolar su envío.
        val id = parametros.id ?: UUID.randomUUID().toString()
        val ahora = System.currentTimeMillis()

        return runCatching {
            repository.guardarLote(
                Lote(
                    id = id,
                    viveroId = parametros.viveroId,
                    nombre = nombre,
                    areaHectareas = parametros.areaHectareas,
                    latitudCentroide = parametros.latitud,
                    longitudCentroide = parametros.longitud,
                    // Toda edición vuelve a estado pendiente: el servidor debe
                    // enterarse del cambio, no solo de la creación.
                    estadoSync = EstadoSync.PENDIENTE,
                    actualizadoEn = ahora
                )
            )
            id
        }
    }

    data class Parametros(
        /** Nulo al crear, presente al editar. */
        val id: String?,
        val viveroId: String,
        val nombre: String,
        val areaHectareas: Double,
        val latitud: Double,
        val longitud: Double
    )

    private companion object {
        const val LONGITUD_MINIMA_NOMBRE = 3

        /**
         * Umbral de cordura, no un límite legal. Los lotes de vivero comunitario
         * rara vez superan unas pocas hectáreas; un valor mayor casi siempre
         * significa que alguien escribió metros cuadrados.
         */
        const val AREA_MAXIMA_HECTAREAS = 500.0
    }
}
