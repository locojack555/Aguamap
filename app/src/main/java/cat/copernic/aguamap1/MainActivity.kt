package cat.copernic.aguamap1

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import cat.copernic.aguamap1.presentation.navigationInitial.NavigationWrapper
import cat.copernic.aguamap1.ui.theme.AguaMap1Theme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration

/**
 * Actividad principal de la aplicación que actúa como contenedor único (Single Activity).
 * Se encarga de la configuración global del sistema de mapas, la inyección de dependencias
 * y la inicialización del grafo de navegación de Compose.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    /**
     * Ciclo de vida inicial. Configura el entorno antes de renderizar la UI.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Establece el tema definido en XML antes de cargar Compose para evitar parpadeos
        setTheme(R.style.Theme_AguaMap1)

        // Configuración necesaria para OSMDroid (OpenStreetMap)
        Configuration.getInstance().apply {
            // Requerido por la política de uso de OSM para identificar la app en sus servidores
            userAgentValue = packageName
            // Carga la configuración de caché y parámetros del mapa desde preferencias
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        }

        super.onCreate(savedInstanceState)

        // Configuración visual: permite que la app se dibuje bajo las barras de sistema
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        // Punto de entrada de Jetpack Compose
        setContent {
            // Inicializa el controlador de navegación que gestionará los cambios de pantalla
            val navHostController = rememberNavController()

            AguaMap1Theme {
                // Superficie base que ocupa toda la pantalla y aplica los colores del tema
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Wrapper que contiene todas las rutas y destinos de la aplicación
                    NavigationWrapper(navHostController)
                }
            }
        }
    }
}