package co.edu.ucn.retono.ui.screens.registro

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.ucn.retono.ui.theme.Clay
import co.edu.ucn.retono.ui.theme.Foliage
import co.edu.ucn.retono.ui.theme.Ochre

@Composable
fun RegistroScreen(viewModel: RegistroViewModel = hiltViewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val especies by viewModel.especies.collectAsStateWithLifecycle()
    val nombreLote by viewModel.nombreLoteActivo.collectAsStateWithLifecycle()

    // El archivo destino se recuerda entre la solicitud a la cámara y su
    // respuesta: el contrato TakePicture solo devuelve si tuvo éxito, no dónde
    // escribió.
    var archivoPendiente by remember { mutableStateOf<File?>(null) }

    val tomarFoto = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exitosa ->
        val archivo = archivoPendiente
        if (exitosa && archivo != null) viewModel.procesarFoto(archivo)
        archivoPendiente = null
    }

    val solicitarCamara = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            val (archivo, uri) = viewModel.prepararCaptura()
            archivoPendiente = archivo
            tomarFoto.launch(uri)
        }
    }

    // El permiso se pide en el momento de usarlo, no al abrir la aplicación:
    // así el usuario entiende para qué se solicita.
    val solicitarPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) viewModel.obtenerUbicacion()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Registrar árbol", style = MaterialTheme.typography.headlineMedium)
            Text(
                nombreLote ?: "Sin lote seleccionado · elija uno en la pestaña Lotes",
                style = MaterialTheme.typography.bodyMedium,
                color = if (nombreLote == null) Ochre
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // ---------- Especie ----------
        Text("Especie", style = MaterialTheme.typography.titleMedium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(especies, key = { it.id }) { especie ->
                FilterChip(
                    selected = estado.especieSeleccionada?.id == especie.id,
                    onClick = { viewModel.seleccionarEspecie(especie) },
                    label = { Text(especie.nombreComun) }
                )
            }
        }

        // ---------- Ubicación ----------
        Text("Ubicación", style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                val u = estado.ubicacion
                when {
                    estado.obteniendoUbicacion -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Buscando señal de GPS…")
                    }

                    u != null -> {
                        Text(
                            "%.5f, %.5f".format(u.latitud, u.longitud),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Precisión: ±%.0f m".format(u.precisionMetros),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (estado.precisionInsuficiente) Ochre else Foliage,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (estado.precisionInsuficiente) {
                            Text(
                                "Con esta precisión no será posible reencontrar el árbol " +
                                    "en el próximo monitoreo. Salga a cielo abierto y repita la lectura.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Ochre,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }

                    else -> Text(
                        "Sin lectura todavía",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = { solicitarPermiso.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    enabled = !estado.obteniendoUbicacion,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Text(if (u == null) "Tomar ubicación" else "Repetir lectura")
                }
            }
        }

        // ---------- Fotografía ----------
        Text("Fotografía", style = MaterialTheme.typography.titleMedium)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                when {
                    estado.procesandoFoto -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        Text("Procesando imagen…")
                    }

                    estado.rutaFoto != null -> {
                        AsyncImage(
                            model = estado.rutaFoto,
                            contentDescription = "Fotografía del árbol registrado",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(10.dp))
                        )
                        TextButton(
                            onClick = viewModel::descartarFoto,
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Quitar y repetir")
                        }
                    }

                    else -> Text(
                        "Opcional. La imagen se reduce y se le eliminan los metadatos " +
                            "antes de guardarse.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (estado.rutaFoto == null && !estado.procesandoFoto) {
                    OutlinedButton(
                        onClick = { solicitarCamara.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        Text("Tomar fotografía", Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        // ---------- Observaciones ----------
        OutlinedTextField(
            value = estado.observaciones,
            onValueChange = viewModel::cambiarObservaciones,
            label = { Text("Observaciones (opcional)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        // ---------- Guardar ----------
        Button(
            onClick = viewModel::guardar,
            enabled = estado.puedeGuardar && nombreLote != null,
            colors = ButtonDefaults.buttonColors(containerColor = Foliage),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (estado.guardando) "Guardando…" else "Registrar árbol")
        }

        Text(
            "Se guarda en el teléfono de inmediato. La subida ocurre sola cuando " +
                "aparezca señal, aunque cierre la aplicación.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (estado.registradosEnSesion > 0) {
            Text(
                "${estado.registradosEnSesion} árboles registrados en esta jornada",
                style = MaterialTheme.typography.titleMedium,
                color = Foliage
            )
        }

        estado.mensaje?.let { mensaje ->
            Snackbar(
                containerColor = if (estado.esError) Clay else Foliage,
                action = {
                    Text(
                        "Cerrar",
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable { viewModel.limpiarMensaje() }
                    )
                }
            ) { Text(mensaje) }
        }
    }
}