package co.edu.ucn.retono.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import co.edu.ucn.retono.ui.screens.lotes.LotesScreen
import co.edu.ucn.retono.ui.screens.monitoreo.MonitoreoScreen
import co.edu.ucn.retono.ui.screens.registro.RegistroScreen
import co.edu.ucn.retono.ui.screens.sincronizacion.SincronizacionScreen

sealed class Destino(val ruta: String, val etiqueta: String, val icono: ImageVector) {
    data object Lotes : Destino("lotes", "Lotes", Icons.Filled.Forest)
    data object Registro : Destino("registro", "Registrar", Icons.Filled.AddCircle)
    data object Monitoreo : Destino("monitoreo", "Monitorear", Icons.Filled.ContentPaste)
    data object Sincronizacion : Destino("sync", "Sincronizar", Icons.Filled.CloudUpload)
}

private val destinos =
    listOf(Destino.Lotes, Destino.Registro, Destino.Monitoreo, Destino.Sincronizacion)

@Composable
fun RetonoNavHost(pendientes: Int = 0) {
    val navController = rememberNavController()
    val entradaActual by navController.currentBackStackEntryAsState()
    val destinoActual = entradaActual?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                destinos.forEach { destino ->
                    val seleccionado = destinoActual?.hierarchy?.any {
                        it.route == destino.ruta
                    } == true

                    NavigationBarItem(
                        selected = seleccionado,
                        onClick = {
                            navController.navigate(destino.ruta) {
                                // Evita apilar copias de la misma pantalla al
                                // tocar repetidamente la barra inferior.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            // El contador de pendientes va sobre el icono de
                            // sincronización: el técnico debe poder ver cuánto
                            // trabajo suyo falta por subir sin entrar a mirar.
                            if (destino is Destino.Sincronizacion && pendientes > 0) {
                                BadgedBox(badge = { Badge { Text("$pendientes") } }) {
                                    Icon(destino.icono, contentDescription = null)
                                }
                            } else {
                                Icon(destino.icono, contentDescription = null)
                            }
                        },
                        label = { Text(destino.etiqueta) }
                    )
                }
            }
        }
    ) { relleno ->
        NavHost(
            navController = navController,
            startDestination = Destino.Lotes.ruta,
            modifier = Modifier.padding(relleno)
        ) {
            composable(Destino.Lotes.ruta) { LotesScreen() }
            composable(Destino.Registro.ruta) { RegistroScreen() }
            composable(Destino.Monitoreo.ruta) { MonitoreoScreen() }
            composable(Destino.Sincronizacion.ruta) { SincronizacionScreen() }
        }
    }
}
