package co.edu.ucn.retono.ui.screens.sincronizacion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.ucn.retono.domain.model.EstadoSync
import co.edu.ucn.retono.ui.components.EtiquetaSync
import co.edu.ucn.retono.ui.theme.Clay
import co.edu.ucn.retono.ui.theme.Foliage
import co.edu.ucn.retono.ui.theme.Ochre

@Composable
fun SincronizacionScreen(viewModel: SincronizacionViewModel = hiltViewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (estado.estaAlDia) "Todo sincronizado"
                   else "${estado.totalPendientes} registros esperan conexión",
            style = MaterialTheme.typography.headlineMedium
        )

        // Sin backend configurado, nada pasará nunca a SINCRONIZADO. Callarlo
        // haría parecer que la aplicación está rota cuando en realidad está
        // haciendo exactamente lo que debe: conservar el dato hasta que haya
        // a quién entregárselo.
        if (!viewModel.hayBackendConfigurado) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Ochre.copy(alpha = 0.14f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "No hay servidor configurado",
                        style = MaterialTheme.typography.titleMedium,
                        color = Ochre
                    )
                    Text(
                        "Los registros se guardan en el teléfono y permanecerán " +
                            "pendientes hasta que se agregue app/google-services.json " +
                            "y se recompile. Ningún dato se pierde entretanto.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        if (estado.hayConflictos) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Clay.copy(alpha = 0.14f))
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "${estado.conflictos.size} registros en conflicto",
                        style = MaterialTheme.typography.titleMedium,
                        color = Clay
                    )
                    Text(
                        "El servidor tiene una versión más reciente. Revise cada " +
                            "caso y decida cuál conservar.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        Button(
            onClick = viewModel::sincronizarAhora,
            enabled = !estado.estaAlDia && viewModel.hayBackendConfigurado,
            colors = ButtonDefaults.buttonColors(containerColor = Foliage),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sincronizar ahora")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(estado.pendientes + estado.conflictos, key = { it.id }) { siembra ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            "Siembra ${siembra.id.take(8)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "%.5f, %.5f · ±%.0f m".format(
                                siembra.latitud, siembra.longitud, siembra.precisionMetros
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                        EtiquetaSync(
                            if (siembra.estadoSync == EstadoSync.CONFLICTO)
                                EstadoSync.CONFLICTO else EstadoSync.PENDIENTE
                        )
                    }
                }
            }
        }
    }
}
