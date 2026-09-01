package co.edu.ucn.retono.ui.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.ucn.retono.data.local.PreferenciasApp
import co.edu.ucn.retono.data.location.ProveedorUbicacion
import co.edu.ucn.retono.data.media.GestorFotografias
import co.edu.ucn.retono.domain.repository.LoteRepository
import co.edu.ucn.retono.data.location.Ubicacion
import android.net.Uri
import co.edu.ucn.retono.domain.model.Especie
import co.edu.ucn.retono.domain.repository.EspecieRepository
import co.edu.ucn.retono.domain.usecase.RegistrarSiembra
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EstadoRegistro(
    val especieSeleccionada: Especie? = null,
    val observaciones: String = "",
    val ubicacion: Ubicacion? = null,
    val rutaFoto: String? = null,
    val procesandoFoto: Boolean = false,
    val obteniendoUbicacion: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val esError: Boolean = false,
    val registradosEnSesion: Int = 0
) {
    /**
     * El botón de guardar solo se habilita cuando hay especie y una lectura de
     * GPS con precisión aceptable. Prevenir el error es mejor que reportarlo
     * después: en campo, rehacer un registro cuesta caminar de vuelta.
     */
    val puedeGuardar: Boolean
        get() = especieSeleccionada != null &&
            ubicacion != null &&
            ubicacion.precisionMetros <= RegistrarSiembra.PRECISION_MAXIMA_ACEPTABLE &&
            !guardando

    val precisionInsuficiente: Boolean
        get() = ubicacion != null &&
            ubicacion.precisionMetros > RegistrarSiembra.PRECISION_MAXIMA_ACEPTABLE
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RegistroViewModel @Inject constructor(
    especieRepository: EspecieRepository,
    loteRepository: LoteRepository,
    private val preferencias: PreferenciasApp,
    private val proveedorUbicacion: ProveedorUbicacion,
    private val gestorFotografias: GestorFotografias,
    private val registrarSiembra: RegistrarSiembra
) : ViewModel() {

    /** Nombre del lote activo, para que el usuario vea dónde está registrando. */
    val nombreLoteActivo: StateFlow<String?> = preferencias.loteActivo
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else loteRepository.observarLote(id).map { it?.nombre }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _estado = MutableStateFlow(EstadoRegistro())
    val estado: StateFlow<EstadoRegistro> = _estado.asStateFlow()

    val especies: StateFlow<List<Especie>> = especieRepository.observarEspecies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun seleccionarEspecie(especie: Especie) =
        _estado.update { it.copy(especieSeleccionada = especie) }

    fun cambiarObservaciones(texto: String) =
        _estado.update { it.copy(observaciones = texto) }

    fun limpiarMensaje() = _estado.update { it.copy(mensaje = null, esError = false) }

    /** Entrega a la cámara el destino donde debe escribir. */
    fun prepararCaptura(): Pair<java.io.File, Uri> = gestorFotografias.prepararDestino()

    fun descartarFoto() = _estado.update { it.copy(rutaFoto = null) }

    /**
     * Procesa la imagen que dejó la cámara: la reescala, corrige su orientación
     * y la recomprime sin metadatos.
     */
    fun procesarFoto(archivo: java.io.File) {
        _estado.update { it.copy(procesandoFoto = true) }

        viewModelScope.launch {
            gestorFotografias.procesar(archivo)
                .onSuccess { ruta ->
                    _estado.update { it.copy(rutaFoto = ruta, procesandoFoto = false) }
                }
                .onFailure { error ->
                    _estado.update {
                        it.copy(
                            procesandoFoto = false,
                            mensaje = error.message ?: "No se pudo procesar la fotografía",
                            esError = true
                        )
                    }
                }
        }
    }

    fun obtenerUbicacion() {
        _estado.update { it.copy(obteniendoUbicacion = true, mensaje = null) }

        viewModelScope.launch {
            proveedorUbicacion.obtenerUbicacion()
                .onSuccess { ubicacion ->
                    _estado.update {
                        it.copy(ubicacion = ubicacion, obteniendoUbicacion = false)
                    }
                }
                .onFailure { error ->
                    _estado.update {
                        it.copy(
                            obteniendoUbicacion = false,
                            mensaje = error.message ?: "No fue posible leer el GPS",
                            esError = true
                        )
                    }
                }
        }
    }

    fun guardar() {
        val actual = _estado.value
        val especie = actual.especieSeleccionada ?: return
        val ubicacion = actual.ubicacion ?: return

        val loteId = preferencias.loteActivo.value
        if (loteId == null) {
            _estado.update {
                it.copy(
                    mensaje = "Seleccione un lote en la pestaña Lotes antes de registrar.",
                    esError = true
                )
            }
            return
        }

        _estado.update { it.copy(guardando = true) }

        viewModelScope.launch {
            val resultado = registrarSiembra(
                RegistrarSiembra.Parametros(
                    loteId = loteId,
                    especieId = especie.id,
                    latitud = ubicacion.latitud,
                    longitud = ubicacion.longitud,
                    precisionMetros = ubicacion.precisionMetros,
                    rutaFotoLocal = actual.rutaFoto,
                    observaciones = actual.observaciones
                )
            )

            resultado
                .onSuccess {
                    // Se limpia la ubicación pero se conserva la especie: en una
                    // jornada real se siembran varios individuos de la misma
                    // especie seguidos, y volver a escogerla cada vez sería
                    // trabajo repetido innecesario.
                    _estado.update {
                        it.copy(
                            guardando = false,
                            ubicacion = null,
                            observaciones = "",
                            rutaFoto = null,
                            registradosEnSesion = it.registradosEnSesion + 1,
                            mensaje = "Árbol registrado. Se enviará cuando haya señal.",
                            esError = false
                        )
                    }
                }
                .onFailure { error ->
                    _estado.update {
                        it.copy(
                            guardando = false,
                            mensaje = error.message ?: "No se pudo guardar",
                            esError = true
                        )
                    }
                }
        }
    }
}
