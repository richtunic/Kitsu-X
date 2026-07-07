package eu.kanade.presentation.more.settings.screen.about

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import eu.kanade.presentation.components.AppBar
import eu.kanade.presentation.util.Screen
import tachiyomi.presentation.core.components.material.Scaffold

class KitsuXHelpScreen : Screen() {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val faqs = remember {
            listOf(
                FaqItem(
                    question = "1. Las extensiones no se pueden instalar o fallan",
                    answer =
                    "Android bloquea por defecto la instalación de aplicaciones externas. Para solucionarlo:\n\n" +
                        "• Ve a los Ajustes de Android -> Aplicaciones -> Acceso especial -> " +
                        "Instalar aplicaciones desconocidas, y activa el permiso para KitsuX.\n" +
                        "• Asegúrate de haber configurado los repositorios en Configuración -> Experiencia.",
                ),
                FaqItem(
                    question = "2. Error de Cloudflare / HTTP 403",
                    answer = "Las fuentes/servidores usan protección contra bots para evitar saturaciones. " +
                        "Para resolverlo:\n\n" +
                        "• Entra a la ficha de la serie que da el error.\n" +
                        "• Toca el botón de tres puntos superiores y selecciona " +
                        "'Abrir en WebView'.\n" +
                        "• Resuelve el captcha si es necesario y espera a que cargue la página.",
                ),
                FaqItem(
                    question = "3. El video se congela, tiene pantalla negra o audio desincronizado",
                    answer =
                    "Esto sucede por incompatibilidades con la decodificación por hardware de tu dispositivo. " +
                        "Prueba lo siguiente:\n\n" +
                        "• Ve a Configuración -> Reproductor y cambia la opción " +
                        "'Decodificación por hardware' a 'Software'.\n" +
                        "• Activa el uso de un reproductor externo como VLC o MPV en la configuración.",
                ),
                FaqItem(
                    question = "4. Error al importar o exportar copias de seguridad",
                    answer =
                    "Los archivos de backup pueden corromperse o dar error si cambias de arquitectura o versión. " +
                        "Solución:\n\n" +
                        "• Asegúrate de que ambas instalaciones estén actualizadas a la misma versión.\n" +
                        "• Guarda el archivo en el almacenamiento interno en lugar de carpetas protegidas del sistema.",
                ),
                FaqItem(
                    question = "5. No se actualiza la biblioteca o fallan las descargas automáticas",
                    answer = "Android restringe los procesos en segundo plano para ahorrar batería. Solución:\n\n" +
                        "• Entra a los ajustes de batería de tu dispositivo.\n" +
                        "• Busca KitsuX y marca la opción 'Sin restricciones' u optimización de batería desactivada.",
                ),
            )
        }

        Scaffold(
            topBar = {
                AppBar(
                    title = "Ayuda y Soporte KitsuX",
                    navigateUp = { navigator.pop() },
                )
            },
        ) { contentPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = "Problemas Frecuentes",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }
                items(faqs) { faq ->
                    FaqCard(faq)
                }
            }
        }
    }
}

private data class FaqItem(
    val question: String,
    val answer: String,
)

@Composable
private fun FaqCard(faq: FaqItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = faq.question,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                )
            }
            if (expanded) {
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
