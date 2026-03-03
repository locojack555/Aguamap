package cat.copernic.aguamap1

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect // Importante
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // Importante
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import cat.copernic.aguamap1.data.DatabasePopulator // Asegúrate de importar tu clase
import cat.copernic.aguamap1.presentation.navigationInitial.NavigationWrapper
import cat.copernic.aguamap1.ui.theme.AguaMap1Theme
import dagger.hilt.android.AndroidEntryPoint
import org.osmdroid.config.Configuration

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_AguaMap1)

        Configuration.getInstance().apply {
            userAgentValue = packageName
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
        }

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            val navHostController = rememberNavController()
            val context = LocalContext.current // Obtenemos el contexto para la importación

            AguaMap1Theme {
                Surface(modifier = Modifier.fillMaxSize()) {

                    /*// --- BLOQUE TEMPORAL DE IMPORTACIÓN ---
                    // El key = Unit asegura que solo se ejecute al arrancar la App
                    LaunchedEffect(Unit) {
                        // Descomenta la línea de abajo para ejecutar la importación
                        DatabasePopulator.importTerrassaFountains(context)
                    }
                    // ---------------------------------------*/

                    NavigationWrapper(navHostController)
                }
            }
        }
    }
}