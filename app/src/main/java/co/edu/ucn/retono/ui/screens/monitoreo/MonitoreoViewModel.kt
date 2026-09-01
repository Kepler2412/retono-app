package co.edu.ucn.retono.ui.screens.monitoreo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.ucn.retono.data.local.PreferenciasApp
import co.edu.ucn.retono.domain.model.EstadoVital
import co.edu.ucn.retono.domain.model.Monitoreo
import co.edu.ucn.retono.domain.model.Siembra
import co.edu.ucn.retono.domain.repository.EspecieRepository
import co.edu.ucn.retono.domain.repository.MonitoreoRepository
import co.edu.ucn.retono.domain.repository.SiembraRepository
import co.edu.ucn.retono.domain.usecase.RegistrarMonitoreo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Un individuo con su especie y el resultado de su última visita. */
data class IndividuoMonitoreable(
    val siembra: Siembra,
    val nombreEspecie: String,
    val ultimoMonitoreo: Monitoreo?
)

data class EstadoFormulario(
    val siembraId: String? = null,
    val estadoVital: EstadoVital = EstadoVital.VIVO,
    val altura: String = "",
    val diametro: String = "",
    val observaciones: String = "",
    val guardando: Boolean = false,
    val error: String? = null
) {
    val abierto: Boolean get() = siembraId != null

    /** Solo un individuo vivo admite medidas: un muerto no tiene altura. */
    val admiteMedidas: Boolean get() = estadoVital == EstadoVital.VIVO
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MonitoreoViewModel @Inject constructor(
    siembraRepository: SiembraRepository,
    monitoreoRepository: MonitoreoRepository,
    especieRepository: EspecieRepository,
    preferencias: PreferenciasApp,
    private val registrarMonitoreo: RegistrarMonitoreo
) : ViewModel() {

    /**
     * La lista sigue al lote activo: al cambiarlo en la pestaña Lotes, esta
     * pantalla se recompone sola con los individuos del lote nuevo.
     */
    val individuos: StateFlow<List<IndividuoMonitoreable>> = preferencias.loteActivo
        .flatMapLatest { loteId ->
            if (loteId == null) return@flatMapLatest flowOf(emptyList())

            combine(
                siembraRepository.observarSiembrasDeLote(loteId),
                monitoreoRepository.observarMonitoreosDeLote(loteId),
                especieRepository.observarEspecies()
            ) { siembras, monitoreos, especies ->

                val nombrePorEspecie = especies.associate { it.id to it.nombreComun }
                val ultimoPorIndividuo = monitoreos
                    .groupBy { it.siembraId }
                    .mapValues { (_, visitas) -> visitas.maxBy { it.fecha } }

                siembras.map { siembra ->
                    IndividuoMonitoreable(
                        siembra = siembra,
                        nombreEspecie = nombrePorEspecie[siembra.especieId]
                            ?: "Especie desconocida",
                        ultimoMonitoreo = ultimoPorIndividuo[siembra.id]
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _formulario = MutableStateFlow(EstadoFormulario())
    val formulario: StateFlow<EstadoFormulario> = _formulario.asStateFlow()

    fun abrirFormulario(siembraId: String) {
        // Cuerpo de bloque y no de expresión: en Kotlin una asignación es una
        // sentencia, no una expresión, y no puede ir tras el signo igual.
        _formulario.value = EstadoFormulario(siembraId = siembraId)
    }

    fun cerrarFormulario() = _formulario.update { EstadoFormulario() }

    fun cambiarEstadoVital(estado: EstadoVital) = _formulario.update {
        // Al marcar muerto o no encontrado se limpian las medidas: conservarlas
        // produciría el contrasentido de un árbol muerto de 180 cm.
        if (estado == EstadoVital.VIVO) it.copy(estadoVital = estado, error = null)
        else it.copy(estadoVital = estado, altura = "", diametro = "", error = null)
    }

    fun cambiarAltura(valor: String) = _formulario.update { it.copy(altura = valor) }
    fun cambiarDiametro(valor: String) = _formulario.update { it.copy(diametro = valor) }
    fun cambiarObservaciones(valor: String) = _formulario.update { it.copy(observaciones = valor) }

    fun guardar() {
        val actual = _formulario.value
        val siembraId = actual.siembraId ?: return

        _formulario.update { it.copy(guardando = true, error = null) }

        viewModelScope.launch {
            val resultado = registrarMonitoreo(
                RegistrarMonitoreo.Parametros(
                    siembraId = siembraId,
                    estadoVital = actual.estadoVital,
                    alturaCm = actual.altura.replace(',', '.').toDoubleOrNull(),
                    diametroMm = actual.diametro.replace(',', '.').toDoubleOrNull(),
                    observaciones = actual.observaciones
                )
            )

            resultado
                .onSuccess { cerrarFormulario() }
                .onFailure { error ->
                    _formulario.update {
                        it.copy(guardando = false, error = error.message ?: "No se pudo guardar")
                    }
                }
        }
    }
}
