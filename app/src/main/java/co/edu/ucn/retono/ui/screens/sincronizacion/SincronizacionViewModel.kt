package co.edu.ucn.retono.ui.screens.sincronizacion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.ucn.retono.BuildConfig
import co.edu.ucn.retono.data.sync.PlanificadorSync
import co.edu.ucn.retono.domain.model.EstadoSync
import co.edu.ucn.retono.domain.model.Siembra
import co.edu.ucn.retono.domain.repository.SiembraRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Estado de la pantalla de sincronización.
 *
 * Esta pantalla existe por una razón de diseño explícita: el usuario debe poder
 * ver en todo momento qué información suya todavía no llegó al servidor. Ocultar
 * eso produciría la peor falla posible en este dominio: creer que el trabajo de
 * una jornada está a salvo cuando no lo está.
 */
data class EstadoSincronizacion(
    val pendientes: List<Siembra> = emptyList(),
    val conflictos: List<Siembra> = emptyList()
) {
    val totalPendientes: Int get() = pendientes.size
    val hayConflictos: Boolean get() = conflictos.isNotEmpty()
    val estaAlDia: Boolean get() = pendientes.isEmpty() && conflictos.isEmpty()
}

@HiltViewModel
class SincronizacionViewModel @Inject constructor(
    repository: SiembraRepository,
    private val planificador: PlanificadorSync
) : ViewModel() {

    val estado: StateFlow<EstadoSincronizacion> =
        repository.observarPendientesDeSync()
            .map { lista ->
                EstadoSincronizacion(
                    pendientes = lista.filter { it.estadoSync == EstadoSync.PENDIENTE },
                    conflictos = lista.filter { it.estadoSync == EstadoSync.CONFLICTO }
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = EstadoSincronizacion()
            )

    /**
     * Sin google-services.json el proyecto corre en modo local. Se detecta para
     * avisar al usuario en lugar de dejarlo pulsando un botón que nunca hará
     * nada.
     */
    val hayBackendConfigurado: Boolean = BuildConfig.FIREBASE_HABILITADO

    /** Permite forzar un intento manual sin esperar al planificador. */
    fun sincronizarAhora() = planificador.encolarSincronizacion()
}
