package co.edu.ucn.retono.ui.screens.lotes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.ucn.retono.data.local.DatosIniciales
import co.edu.ucn.retono.data.local.PreferenciasApp
import co.edu.ucn.retono.data.location.ProveedorUbicacion
import co.edu.ucn.retono.domain.model.Lote
import co.edu.ucn.retono.domain.model.ResultadoSupervivencia
import co.edu.ucn.retono.domain.repository.LoteRepository
import co.edu.ucn.retono.domain.repository.SiembraRepository
import co.edu.ucn.retono.domain.usecase.CalcularSupervivenciaLote
import co.edu.ucn.retono.domain.usecase.GuardarLote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoteConIndicador(
    val lote: Lote,
    val totalSiembras: Int,
    val supervivencia: ResultadoSupervivencia?
)

data class EstadoFormularioLote(
    val abierto: Boolean = false,
    /** Nulo al crear, presente al editar. */
    val idEnEdicion: String? = null,
    val nombre: String = "",
    val area: String = "",
    val latitud: String = "",
    val longitud: String = "",
    val obteniendoUbicacion: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null
) {
    val esEdicion: Boolean get() = idEnEdicion != null
    val puedeGuardar: Boolean
        get() = nombre.trim().length >= 3 &&
            area.replace(',', '.').toDoubleOrNull() != null &&
            latitud.replace(',', '.').toDoubleOrNull() != null &&
            longitud.replace(',', '.').toDoubleOrNull() != null &&
            !guardando
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LotesViewModel @Inject constructor(
    loteRepository: LoteRepository,
    siembraRepository: SiembraRepository,
    calcularSupervivencia: CalcularSupervivenciaLote,
    private val guardarLote: GuardarLote,
    private val preferencias: PreferenciasApp,
    private val proveedorUbicacion: ProveedorUbicacion
) : ViewModel() {

    val loteActivo: StateFlow<String?> = preferencias.loteActivo

    val lotes: StateFlow<List<LoteConIndicador>> =
        loteRepository.observarLotes(DatosIniciales.vivero.id)
            .flatMapLatest { listaLotes ->
                if (listaLotes.isEmpty()) return@flatMapLatest flowOf(emptyList())

                val flujos = listaLotes.map { lote ->
                    combine(
                        siembraRepository.observarSiembrasDeLote(lote.id),
                        calcularSupervivencia(lote.id)
                    ) { siembras, supervivencia ->
                        LoteConIndicador(
                            lote = lote,
                            totalSiembras = siembras.size,
                            supervivencia = supervivencia.takeIf { it.observados > 0 }
                        )
                    }
                }

                combine(flujos) { it.toList() }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _formulario = MutableStateFlow(EstadoFormularioLote())
    val formulario: StateFlow<EstadoFormularioLote> = _formulario.asStateFlow()

    fun seleccionarLote(loteId: String) = preferencias.seleccionarLote(loteId)

    fun abrirCreacion() {
        _formulario.value = EstadoFormularioLote(abierto = true)
    }

    fun abrirEdicion(lote: Lote) {
        _formulario.value = EstadoFormularioLote(
            abierto = true,
            idEnEdicion = lote.id,
            nombre = lote.nombre,
            area = lote.areaHectareas.toString(),
            latitud = lote.latitudCentroide.toString(),
            longitud = lote.longitudCentroide.toString()
        )
    }

    fun cerrarFormulario() {
        _formulario.value = EstadoFormularioLote()
    }

    fun cambiarNombre(valor: String) = _formulario.update { it.copy(nombre = valor, error = null) }
    fun cambiarArea(valor: String) = _formulario.update { it.copy(area = valor, error = null) }
    fun cambiarLatitud(valor: String) = _formulario.update { it.copy(latitud = valor) }
    fun cambiarLongitud(valor: String) = _formulario.update { it.copy(longitud = valor) }

    /** Rellena el centroide con la posición actual, estando parado en el lote. */
    fun usarUbicacionActual() {
        _formulario.update { it.copy(obteniendoUbicacion = true, error = null) }

        viewModelScope.launch {
            proveedorUbicacion.obtenerUbicacion()
                .onSuccess { ubicacion ->
                    _formulario.update {
                        it.copy(
                            latitud = ubicacion.latitud.toString(),
                            longitud = ubicacion.longitud.toString(),
                            obteniendoUbicacion = false
                        )
                    }
                }
                .onFailure { error ->
                    _formulario.update {
                        it.copy(obteniendoUbicacion = false, error = error.message)
                    }
                }
        }
    }

    fun guardar() {
        val actual = _formulario.value
        _formulario.update { it.copy(guardando = true, error = null) }

        viewModelScope.launch {
            val resultado = guardarLote(
                GuardarLote.Parametros(
                    id = actual.idEnEdicion,
                    viveroId = DatosIniciales.vivero.id,
                    nombre = actual.nombre,
                    areaHectareas = actual.area.replace(',', '.').toDoubleOrNull() ?: 0.0,
                    latitud = actual.latitud.replace(',', '.').toDoubleOrNull() ?: 0.0,
                    longitud = actual.longitud.replace(',', '.').toDoubleOrNull() ?: 0.0
                )
            )

            resultado
                .onSuccess { id ->
                    // Un lote recién creado pasa a ser el activo: quien lo crea
                    // es porque va a empezar a registrar allí.
                    if (!actual.esEdicion) preferencias.seleccionarLote(id)
                    cerrarFormulario()
                }
                .onFailure { error ->
                    _formulario.update {
                        it.copy(guardando = false, error = error.message ?: "No se pudo guardar")
                    }
                }
        }
    }
}
