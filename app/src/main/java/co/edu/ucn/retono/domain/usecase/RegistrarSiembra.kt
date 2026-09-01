package co.edu.ucn.retono.domain.usecase

import co.edu.ucn.retono.domain.model.EstadoSync
import co.edu.ucn.retono.domain.model.Siembra
import co.edu.ucn.retono.domain.repository.SiembraRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Registra un individuo sembrado.
 *
 * El identificador se genera en el cliente como UUID v4. Esta decisión es la que
 * hace posible que varios dispositivos registren en campo sin coordinación previa
 * y sin colisiones de clave primaria al sincronizar. Delegar el id al servidor
 * obligaría a esperar conectividad para crear el registro, que es exactamente lo
 * que el proyecto busca evitar.
 */
class RegistrarSiembra @Inject constructor(
    private val repository: SiembraRepository
) {

    suspend operator fun invoke(parametros: Parametros): Result<String> {
        if (parametros.precisionMetros > PRECISION_MAXIMA_ACEPTABLE) {
            return Result.failure(
                PrecisionInsuficienteException(parametros.precisionMetros)
            )
        }

        val id = UUID.randomUUID().toString()
        val ahora = System.currentTimeMillis()

        // La escritura se envuelve en runCatching a propósito. Una violación de
        // llave foránea o un fallo de disco lanzaría una excepción que, al no
        // atraparse, escaparía del viewModelScope y cerraría la aplicación en
        // pleno campo. Es preferible devolver el error y mostrarlo en pantalla.
        return runCatching {
            repository.registrarSiembra(
                Siembra(
                    id = id,
                    loteId = parametros.loteId,
                    especieId = parametros.especieId,
                    latitud = parametros.latitud,
                    longitud = parametros.longitud,
                    precisionMetros = parametros.precisionMetros,
                    fechaSiembra = ahora,
                    rutaFotoLocal = parametros.rutaFotoLocal,
                    observaciones = parametros.observaciones,
                    estadoSync = EstadoSync.PENDIENTE,
                    actualizadoEn = ahora
                )
            )
            id
        }
    }

    data class Parametros(
        val loteId: String,
        val especieId: String,
        val latitud: Double,
        val longitud: Double,
        val precisionMetros: Float,
        val rutaFotoLocal: String?,
        val observaciones: String
    )

    class PrecisionInsuficienteException(val precision: Float) : Exception(
        "La precisión del GPS es de ${precision} m. Espere una lectura mejor: " +
            "por encima de $PRECISION_MAXIMA_ACEPTABLE m no es posible " +
            "reencontrar el individuo en el próximo monitoreo."
    )

    companion object {
        /**
         * Umbral derivado del problema, no arbitrario: con separaciones típicas de
         * 3 m entre individuos, una precisión peor que 15 m impide distinguir un
         * árbol de sus vecinos en la visita siguiente.
         */
        const val PRECISION_MAXIMA_ACEPTABLE = 15f
    }
}
