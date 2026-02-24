package cat.copernic.aguamap1

import android.app.Application
import com.cloudinary.android.MediaManager

import dagger.hilt.android.HiltAndroidApp
import java.util.HashMap

@HiltAndroidApp
class AguaMapApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Inicializar Cloudinary con tus credenciales
        // REGISTRATE EN https://cloudinary.com y obtén estos datos
        val config = HashMap<String, Any>().apply {
            put("cloud_name", "dghwhasty") // Reemplaza con tu cloud_name
            put("secure", true)
        }

        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            // Si ya está inicializado, ignoramos
        }
    }
}