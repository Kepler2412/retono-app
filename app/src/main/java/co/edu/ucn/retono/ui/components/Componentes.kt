package co.edu.ucn.retono.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.edu.ucn.retono.domain.model.EstadoSync
import co.edu.ucn.retono.ui.theme.Clay
import co.edu.ucn.retono.ui.theme.Moss
import co.edu.ucn.retono.ui.theme.Ochre

/**
 * Indicador del estado de sincronización de un registro.
 *
 * Aparece en cada elemento de lista, no solo en la pantalla de sincronización:
 * el usuario debe saber en todo momento si un dato suyo ya está a salvo.
 * El texto acompaña al color porque el color solo no es accesible.
 */
@Composable
fun EtiquetaSync(estado: EstadoSync, modifier: Modifier = Modifier) {
    val (color, texto) = when (estado) {
        EstadoSync.PENDIENTE -> Ochre to "Pendiente"
        EstadoSync.SINCRONIZADO -> Moss to "Sincronizado"
        EstadoSync.CONFLICTO -> Clay to "En conflicto"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = colorTextoLegible(color)
        )
    }
}

/** El ocre sobre fondo claro necesita oscurecerse para mantener contraste. */
private fun colorTextoLegible(base: Color): Color =
    Color(
        red = base.red * 0.75f,
        green = base.green * 0.75f,
        blue = base.blue * 0.75f,
        alpha = 1f
    )
