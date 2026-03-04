package cat.copernic.aguamap1

import android.app.Application
import com.cloudinary.android.MediaManager
import dagger.hilt.android.HiltAndroidApp
import java.util.HashMap

/**
 * Punto de entrada base de la aplicación AguaMap.
 * * Esta clase inicializa el grafo de dependencias de Dagger Hilt y configura
 * los servicios de terceros necesarios antes de que se cree cualquier actividad o servicio.
 */
@HiltAndroidApp
class AguaMapApp : Application() {

    /**
     * Se ejecuta una única vez cuando la aplicación se inicia.
     * Ideal para configuraciones globales y SDKs.
     */
    override fun onCreate() {
        super.onCreate()

        // Configuración de Cloudinary para la subida y optimización de imágenes (fotos de fuentes)
        val config = HashMap<String, Any>().apply {
            put("cloud_name", "dghwhasty") // Identificador único del almacenamiento en la nube
            put("secure", true)           // Fuerza el uso de HTTPS para todas las transferencias
        }

        try {
            // Inicializa el MediaManager de Cloudinary de forma global
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // MediaManager solo permite una inicialización por ciclo de vida de la app.
            // Capturamos la excepción para evitar cierres inesperados en reinicios en caliente.
        }
    }
}