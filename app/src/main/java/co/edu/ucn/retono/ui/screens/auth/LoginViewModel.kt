package co.edu.ucn.retono.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.ucn.retono.data.auth.RepositorioAutenticacion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EstadoLogin(
    val correo: String = "",
    val contrasena: String = "",
    val modoRegistro: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null
) {
    val puedeEnviar: Boolean
        get() = correo.contains("@") && contrasena.length >= 6 && !cargando
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val autenticacion: RepositorioAutenticacion
) : ViewModel() {

    private val _estado = MutableStateFlow(EstadoLogin())
    val estado: StateFlow<EstadoLogin> = _estado.asStateFlow()

    fun cambiarCorreo(valor: String) = _estado.update { it.copy(correo = valor, error = null) }
    fun cambiarContrasena(valor: String) = _estado.update { it.copy(contrasena = valor, error = null) }
    fun alternarModo() = _estado.update { it.copy(modoRegistro = !it.modoRegistro, error = null) }

    fun enviar() {
        val actual = _estado.value
        _estado.update { it.copy(cargando = true, error = null) }

        viewModelScope.launch {
            val resultado =
                if (actual.modoRegistro) autenticacion.registrar(actual.correo, actual.contrasena)
                else autenticacion.iniciarSesion(actual.correo, actual.contrasena)

            resultado.onFailure { error ->
                _estado.update { it.copy(cargando = false, error = traducir(error)) }
            }
            // En caso de éxito no se toca el estado: el AuthStateListener cambia
            // la navegación y esta pantalla desaparece.
        }
    }

    /**
     * Firebase devuelve mensajes en inglés y con jerga técnica. Se traducen a
     * algo accionable, distinguiendo el fallo de red porque es el más probable
     * en el contexto del proyecto.
     */
    private fun traducir(error: Throwable): String {
        val mensaje = error.message.orEmpty()
        return when {
            mensaje.contains("network", ignoreCase = true) ->
                "Sin conexión. El primer inicio de sesión necesita internet; " +
                    "después podrá trabajar sin red."
            mensaje.contains("password is invalid", ignoreCase = true) ||
                mensaje.contains("credential is incorrect", ignoreCase = true) ->
                "Correo o contraseña incorrectos."
            mensaje.contains("email address is already in use", ignoreCase = true) ->
                "Ese correo ya tiene una cuenta. Inicie sesión."
            mensaje.contains("no user record", ignoreCase = true) ->
                "No existe una cuenta con ese correo. Regístrese primero."
            else -> mensaje.ifBlank { "No fue posible autenticar" }
        }
    }
}
