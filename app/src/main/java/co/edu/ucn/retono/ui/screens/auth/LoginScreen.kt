package co.edu.ucn.retono.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import co.edu.ucn.retono.ui.theme.Clay
import co.edu.ucn.retono.ui.theme.Foliage

@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Retoño", style = MaterialTheme.typography.headlineMedium, color = Foliage)
        Text(
            "Inventario forestal de viveros comunitarios",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, bottom = 32.dp)
        )

        OutlinedTextField(
            value = estado.correo,
            onValueChange = viewModel::cambiarCorreo,
            label = { Text("Correo") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.contrasena,
            onValueChange = viewModel::cambiarContrasena,
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        )

        estado.error?.let { mensaje ->
            Text(
                mensaje,
                style = MaterialTheme.typography.bodyMedium,
                color = Clay,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Button(
            onClick = viewModel::enviar,
            enabled = estado.puedeEnviar,
            colors = ButtonDefaults.buttonColors(containerColor = Foliage),
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
        ) {
            Text(
                when {
                    estado.cargando -> "Un momento…"
                    estado.modoRegistro -> "Crear cuenta"
                    else -> "Entrar"
                }
            )
        }

        TextButton(onClick = viewModel::alternarModo, modifier = Modifier.padding(top = 8.dp)) {
            Text(
                if (estado.modoRegistro) "Ya tengo cuenta"
                else "No tengo cuenta, quiero registrarme"
            )
        }

        Text(
            "El inicio de sesión requiere internet solo la primera vez. " +
                "Después podrá registrar árboles sin conexión durante días.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 28.dp)
        )
    }
}
