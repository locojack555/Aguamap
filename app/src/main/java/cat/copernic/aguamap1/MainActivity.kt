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
import org.osmdroid.config.Configuration // Importante

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_AguaMap1)

        // 1. CONFIGURACIÓN DE OSMDROID (Antes de super.onCreate y setContent)
        // Esto soluciona los errores de "User Agent" y permite la descarga de mapas
        Configuration.getInstance().apply {
            // Identifica tu app ante los servidores de OpenStreetMap
            userAgentValue = packageName

            // Carga la configuración de caché para evitar el fondo gris en el futuro
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        }

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            val navHostController = rememberNavController()
            AguaMap1Theme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavigationWrapper(navHostController)
                }
            }
        }
    }
}