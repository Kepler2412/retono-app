package co.edu.ucn.retono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import co.edu.ucn.retono.data.auth.RepositorioAutenticacion
import co.edu.ucn.retono.domain.repository.SiembraRepository
import co.edu.ucn.retono.ui.navigation.RetonoNavHost
import co.edu.ucn.retono.ui.screens.auth.LoginScreen
import co.edu.ucn.retono.ui.theme.RetonoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var siembraRepository: SiembraRepository
    @Inject lateinit var autenticacion: RepositorioAutenticacion

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val flujoPendientes = siembraRepository.observarPendientesDeSync().map { it.size }

        setContent {
            RetonoTheme {
                val pendientes by flujoPendientes.collectAsState(initial = 0)

                // En modo local (sin google-services.json) no se exige sesión:
                // el proyecto debe poder clonarse y probarse sin credenciales.
                if (!BuildConfig.FIREBASE_HABILITADO) {
                    RetonoNavHost(pendientes = pendientes)
                    return@RetonoTheme
                }

                val sesion by autenticacion.sesion.collectAsState(
                    initial = autenticacion.uidActual()?.let {
                        co.edu.ucn.retono.data.auth.UsuarioSesion(it, null)
                    }
                )

                if (sesion == null) LoginScreen()
                else RetonoNavHost(pendientes = pendientes)
            }
        }
    }
}
