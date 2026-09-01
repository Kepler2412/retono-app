package co.edu.ucn.retono.domain.usecase

import co.edu.ucn.retono.domain.model.EstadoSync
import co.edu.ucn.retono.domain.model.EstadoVital
import co.edu.ucn.retono.domain.model.Monitoreo
import co.edu.ucn.retono.domain.repository.MonitoreoRepository
import java.util.UUID
import javax.inject.Inject

/**
 * Registra una visita de seguimiento a un individuo previamente sembrado.
 *
 * Es la pieza que alimenta el cálculo de supervivencia. Sin monitoreos, el
 * indicador que da sentido al proyecto no tiene datos de entrada.
 */
class RegistrarMonitoreo @Inject constructor(
    private val repository: MonitoreoRepository
) {

    suspend operator fun invoke(parametros: Parametros): Result<String> {
        // Un individuo muerto o no encontrado no tiene medidas que tomar.
        // Aceptarlas produciría datos contradictorios: un árbol muerto de 180 cm.
        if (!parametros.estadoVital.esObservacionValida ||
            parametros.estadoVital == EstadoVital.MUERTO
        ) {
            if (parametros.alturaCm != null || parametros.diametroMm != null) {
                return Result.failure(
                    MedidasIncoherentesException(parametros.estadoVital)
                )
            }
        }

        // Un individuo vivo sin altura no es un error, pero conviene advertirlo:
        // sin medidas no se puede evaluar crecimiento, solo supervivencia.
        if (parametros.estadoVital == EstadoVital.VIVO &&
            parametros.alturaCm != null &&
            parametros.alturaCm <= 0
        ) {
            return Result.failure(
                IllegalArgumentException("La altura debe ser mayor que cero")
            )
        }

        val id = UUID.randomUUID().toString()
        val ahora = System.currentTimeMillis()

        return runCatching {
            repository.registrarMonitoreo(
                Monitoreo(
                    id = id,
                    siembraId = parametros.siembraId,
                    fecha = ahora,
                    estadoVital = parametros.estadoVital,
                    alturaCm = parametros.alturaCm,
                    diametroMm = parametros.diametroMm,
                    observaciones = parametros.observaciones,
                    estadoSync = EstadoSync.PENDIENTE,
                    actualizadoEn = ahora
                )
            )
            id
        }
    }

    data class Parametros(
        val siembraId: String,
        val estadoVital: EstadoVital,
        val alturaCm: Double?,
        val diametroMm: Double?,
        val observaciones: String
    )

    class MedidasIncoherentesException(estado: EstadoVital) : Exception(
        "Un individuo registrado como ${estado.name.lowercase()} no puede tener " +
            "medidas de altura o diámetro."
    )
}
