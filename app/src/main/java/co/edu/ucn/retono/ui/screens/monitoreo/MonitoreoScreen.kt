package co.edu.ucn.retono.ui.screens.monitoreo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.ucn.retono.domain.model.EstadoVital
import co.edu.ucn.retono.ui.theme.Clay
import co.edu.ucn.retono.ui.theme.Foliage
import co.edu.ucn.retono.ui.theme.Ochre
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MonitoreoScreen(viewModel: MonitoreoViewModel = hiltViewModel()) {
    val individuos by viewModel.individuos.collectAsStateWithLifecycle()
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Monitoreo", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Toque un individuo para registrar su visita de seguimiento",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        if (individuos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Todavía no hay árboles registrados en este lote. " +
                        "Registre siembras antes de monitorear.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(individuos, key = { it.siembra.id }) { individuo ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.abrirFormulario(individuo.siembra.id) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                individuo.nombreEspecie,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                individuo.siembra.id.take(8),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val ultimo = individuo.ultimoMonitoreo
                        if (ultimo == null) {
                            Text(
                                "Sin visitas todavía",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ochre,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        } else {
                            val color = when (ultimo.estadoVital) {
                                EstadoVital.VIVO -> Foliage
                                EstadoVital.MUERTO -> Clay
                                EstadoVital.NO_ENCONTRADO -> Ochre
                            }
                            Text(
                                "%s · %s".format(
                                    etiquetaEstado(ultimo.estadoVital),
                                    formatoFecha.format(Date(ultimo.fecha))
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = color,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                            ultimo.alturaCm?.let { altura ->
                                Text(
                                    "Altura: %.0f cm".format(altura),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (formulario.abierto) {
        FormularioMonitoreo(
            estado = formulario,
            onEstadoVital = viewModel::cambiarEstadoVital,
            onAltura = viewModel::cambiarAltura,
            onDiametro = viewModel::cambiarDiametro,
            onObservaciones = viewModel::cambiarObservaciones,
            onGuardar = viewModel::guardar,
            onCerrar = viewModel::cerrarFormulario
        )
    }
}

@Composable
private fun FormularioMonitoreo(
    estado: EstadoFormulario,
    onEstadoVital: (EstadoVital) -> Unit,
    onAltura: (String) -> Unit,
    onDiametro: (String) -> Unit,
    onObservaciones: (String) -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Registrar visita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Estado del individuo", style = MaterialTheme.typography.titleMedium)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    EstadoVital.entries.forEach { opcion ->
                        FilterChip(
                            selected = estado.estadoVital == opcion,
                            onClick = { onEstadoVital(opcion) },
                            label = { Text(etiquetaEstado(opcion)) }
                        )
                    }
                }

                // Las medidas solo aparecen si el individuo está vivo. Ocultarlas
                // en vez de deshabilitarlas evita que alguien las diligencie por
                // inercia y luego se pregunte por qué el guardado falla.
                if (estado.admiteMedidas) {
                    OutlinedTextField(
                        value = estado.altura,
                        onValueChange = onAltura,
                        label = { Text("Altura (cm)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = estado.diametro,
                        onValueChange = onDiametro,
                        label = { Text("Diámetro del tallo (mm)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        "Un individuo que no está vivo no registra medidas.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedTextField(
                    value = estado.observaciones,
                    onValueChange = onObservaciones,
                    label = { Text("Observaciones") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                estado.error?.let { mensaje ->
                    Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = Clay)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onGuardar, enabled = !estado.guardando) {
                Text(if (estado.guardando) "Guardando…" else "Guardar visita")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}

private fun etiquetaEstado(estado: EstadoVital): String = when (estado) {
    EstadoVital.VIVO -> "Vivo"
    EstadoVital.MUERTO -> "Muerto"
    EstadoVital.NO_ENCONTRADO -> "No hallado"
}

private val formatoFecha = SimpleDateFormat("d MMM yyyy", Locale("es", "CO"))
