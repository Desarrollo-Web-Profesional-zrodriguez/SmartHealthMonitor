package mx.utng.smarthealthmonitor.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mx.utng.smarthealthmonitor.ui.theme.SmartHealthMonitorTheme

@Composable
fun TarjetaDato(
    valor: String,                        // "78" o "4,250"
    unidad: String,                       // "bpm" o "pasos"
    label: String,                        // "Frecuencia cardíaca"
    colorValor: Color,                    // MaterialTheme.colorScheme.error
    modifier: Modifier = Modifier,        // ← siempre en primer lugar de los opcionales
    icono: ImageVector? = null,           // Ícono opcional
    estado: String? = null,               // "Normal" o "Consulta al médico"
    colorEstado: Color = Color.Gray       // Color del chip
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (estado != null) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(estado, style = MaterialTheme.typography.labelSmall) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            labelColor = colorEstado,
                            containerColor = colorEstado.copy(alpha = 0.1f)
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = colorEstado,
                            borderWidth = 1.dp
                        )
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (icono != null) {
                    Icon(
                        imageVector = icono,
                        contentDescription = null,
                        tint = colorValor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    text = valor,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = colorValor
                )
                Text(
                    text = unidad,
                    style = MaterialTheme.typography.titleSmall,
                    color = colorValor,
                    modifier = Modifier.padding(top = 8.dp) // Ajuste para alinear con el valor
                )
            }
        }
    }
}

// Preview con datos de prueba
@Preview(showBackground = true, name = "TarjetaDato - FC")
@Composable
private fun TarjetaDatoPreview() {
    SmartHealthMonitorTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TarjetaDato(
                valor = "78", unidad = "bpm",
                label = "Frecuencia cardíaca",
                colorValor = MaterialTheme.colorScheme.error,
                icono = null
            )
            TarjetaDato(
                valor = "4,250", unidad = "pasos",
                label = "Pasos del día",
                colorValor = MaterialTheme.colorScheme.primary,
                icono = Icons.AutoMirrored.Filled.DirectionsWalk
            )
        }
    }
}

