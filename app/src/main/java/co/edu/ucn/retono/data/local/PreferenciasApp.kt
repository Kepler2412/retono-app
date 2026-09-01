package co.edu.ucn.retono.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Preferencias de trabajo del usuario.
 *
 * Guarda cuál es el lote activo: el que las pantallas de registro y monitoreo
 * usan por defecto. Se persiste porque una jornada de campo transcurre entera
 * en el mismo lote, y obligar a reelegirlo cada vez que se abre la aplicación
 * sería trabajo repetido sin motivo.
 *
 * Se usan SharedPreferences planas y no cifradas a propósito: un identificador
 * de lote no es información sensible. El cifrado está donde debe estar, en la
 * base de datos (ver ADR-005).
 */
@Singleton
class PreferenciasApp @Inject constructor(
    private val context: Context
) {

    private val preferencias by lazy {
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
    }

    /**
     * Al primer arranque se toma el lote de demostración de la semilla, para que
     * el usuario pueda registrar de inmediato sin tener que crear un lote antes.
     */
    private val _loteActivo = MutableStateFlow(
        preferencias.getString(LOTE_ACTIVO, null) ?: DatosIniciales.loteDemostracion.id
    )

    /** Emite el lote activo y cada cambio posterior. */
    val loteActivo: StateFlow<String?> = _loteActivo.asStateFlow()

    fun seleccionarLote(loteId: String) {
        preferencias.edit().putString(LOTE_ACTIVO, loteId).apply()
        _loteActivo.value = loteId
    }

    private companion object {
        const val ARCHIVO = "retono_preferencias"
        const val LOTE_ACTIVO = "lote_activo"
    }
}
