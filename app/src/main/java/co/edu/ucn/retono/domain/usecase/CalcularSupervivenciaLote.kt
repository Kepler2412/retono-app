package co.edu.ucn.retono.domain.usecase

import co.edu.ucn.retono.domain.model.EstadoVital
import co.edu.ucn.retono.domain.model.Monitoreo
import co.edu.ucn.retono.domain.model.ResultadoSupervivencia
import co.edu.ucn.retono.domain.repository.MonitoreoRepository
import co.edu.ucn.retono.domain.repository.SiembraRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Calcula la tasa de supervivencia de un lote a partir de los datos locales.
 *
 * Este es el indicador que las entidades financiadoras exigen para verificar la
 * ejecución de un proyecto de restauración. Se calcula en el dispositivo, sin
 * depender del servidor: de ahí que el objetivo del proyecto pueda comprometer
 * una consolidación en menos de 24 horas incluso con conectividad intermitente.
 *
 * Reglas de negocio aplicadas:
 *  1. Solo cuenta el monitoreo más reciente de cada individuo. Un árbol visitado
 *     tres veces aporta una observación, no tres.
 *  2. Los individuos no encontrados se excluyen del denominador. Contarlos como
 *     muertos subestimaría la supervivencia; contarlos como vivos la inflaría.
 *  3. Se reporta la cobertura junto con la tasa: una tasa del 95 % sobre el 30 %
 *     del lote no es un resultado, es una muestra insuficiente.
 */
class CalcularSupervivenciaLote @Inject constructor(
    private val siembraRepository: SiembraRepository,
    private val monitoreoRepository: MonitoreoRepository
) {

    operator fun invoke(loteId: String): Flow<ResultadoSupervivencia> =
        combine(
            siembraRepository.observarSiembrasDeLote(loteId),
            monitoreoRepository.observarMonitoreosDeLote(loteId)
        ) { siembras, monitoreos ->

            val ultimoPorIndividuo: Map<String, Monitoreo> = monitoreos
                .groupBy { it.siembraId }
                .mapValues { (_, visitas) -> visitas.maxBy { it.fecha } }

            val vivos = ultimoPorIndividuo.count { it.value.estadoVital == EstadoVital.VIVO }
            val muertos = ultimoPorIndividuo.count { it.value.estadoVital == EstadoVital.MUERTO }
            val noEncontrados =
                ultimoPorIndividuo.count { it.value.estadoVital == EstadoVital.NO_ENCONTRADO }

            val observados = vivos + muertos

            ResultadoSupervivencia(
                loteId = loteId,
                totalSembrado = siembras.size,
                observados = observados,
                vivos = vivos,
                muertos = muertos,
                noEncontrados = noEncontrados,
                tasaSupervivencia = if (observados == 0) 0.0 else vivos.toDouble() / observados,
                calculadoEn = System.currentTimeMillis()
            )
        }
}
