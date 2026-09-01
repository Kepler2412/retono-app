package co.edu.ucn.retono.ui.screens.lotes

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import co.edu.ucn.retono.domain.model.EstadoSync
import co.edu.ucn.retono.ui.components.EtiquetaSync
import co.edu.ucn.retono.ui.theme.Clay
import co.edu.ucn.retono.ui.theme.Foliage
import co.edu.ucn.retono.ui.theme.Ochre

@Composable
fun LotesScreen(viewModel: LotesViewModel = hiltViewModel()) {
    val lotes by viewModel.lotes.collectAsStateWithLifecycle()
    val loteActivo by viewModel.loteActivo.collectAsStateWithLifecycle()
    val formulario by viewModel.formulario.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::abrirCreacion,
                containerColor = Foliage
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Crear lote")
            }
        }
    ) { relleno ->
        Column(Modifier.fillMaxSize().padding(relleno).padding(16.dp)) {
            Text("Lotes", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Toque un lote para trabajar en él",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            if (lotes.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Aún no hay lotes. Cree el primero con el botón + " +
                            "para empezar a registrar siembras.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
                return@Column
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(lotes, key = { it.lote.id }) { item ->
                    val activo = item.lote.id == loteActivo

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                // El lote activo se distingue con un borde, no
                                // solo con color de fondo: debe ser evidente de
                                // un vistazo dónde se van a guardar los registros.
                                if (activo) Modifier.border(
                                    2.dp, Foliage, RoundedCornerShape(12.dp)
                                ) else Modifier
                            )
                            .clickable { viewModel.seleccionarLote(item.lote.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        item.lote.nombre,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        "%.2f ha · %d árboles".format(
                                            item.lote.areaHectareas, item.totalSiembras
                                        ),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                IconButton(onClick = { viewModel.abrirEdicion(item.lote) }) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Editar lote")
                                }
                            }

                            Row(
                                Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (activo) {
                                    Text(
                                        "Lote activo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Foliage
                                    )
                                }
                                if (item.lote.estadoSync != EstadoSync.SINCRONIZADO) {
                                    EtiquetaSync(item.lote.estadoSync)
                                }
                            }

                            val sup = item.supervivencia
                            if (sup == null) {
                                Text(
                                    "Sin monitoreos todavía",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            } else {
                                Row(
                                    Modifier.fillMaxWidth().padding(top = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Text(
                                        "Supervivencia",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        "%.0f %%".format(sup.tasaSupervivencia * 100),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Foliage
                                    )
                                }

                                LinearProgressIndicator(
                                    progress = { sup.tasaSupervivencia.toFloat() },
                                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                                    color = Foliage
                                )

                                if (!sup.esConfiable) {
                                    Text(
                                        "Cobertura del %.0f %%: aún insuficiente para concluir"
                                            .format(sup.cobertura * 100),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Ochre,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }

                                if (sup.noEncontrados > 0) {
                                    Text(
                                        "${sup.noEncontrados} individuos no encontrados",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Clay,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (formulario.abierto) {
        FormularioLote(
            estado = formulario,
            onNombre = viewModel::cambiarNombre,
            onArea = viewModel::cambiarArea,
            onLatitud = viewModel::cambiarLatitud,
            onLongitud = viewModel::cambiarLongitud,
            onUbicacionActual = viewModel::usarUbicacionActual,
            onGuardar = viewModel::guardar,
            onCerrar = viewModel::cerrarFormulario
        )
    }
}

@Composable
private fun FormularioLote(
    estado: EstadoFormularioLote,
    onNombre: (String) -> Unit,
    onArea: (String) -> Unit,
    onLatitud: (String) -> Unit,
    onLongitud: (String) -> Unit,
    onUbicacionActual: () -> Unit,
    onGuardar: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(if (estado.esEdicion) "Editar lote" else "Nuevo lote") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = estado.nombre,
                    onValueChange = onNombre,
                    label = { Text("Nombre del lote") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = estado.area,
                    onValueChange = onArea,
                    label = { Text("Área (hectáreas)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    "Centroide del lote",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = estado.latitud,
                        onValueChange = onLatitud,
                        label = { Text("Latitud") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = estado.longitud,
                        onValueChange = onLongitud,
                        label = { Text("Longitud") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedButton(
                    onClick = onUbicacionActual,
                    enabled = !estado.obteniendoUbicacion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (estado.obteniendoUbicacion) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Filled.MyLocation, contentDescription = null)
                    }
                    Text("Usar mi ubicación", Modifier.padding(start = 8.dp))
                }

                estado.error?.let { mensaje ->
                    Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = Clay)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onGuardar, enabled = estado.puedeGuardar) {
                Text(if (estado.guardando) "Guardando…" else "Guardar")
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
