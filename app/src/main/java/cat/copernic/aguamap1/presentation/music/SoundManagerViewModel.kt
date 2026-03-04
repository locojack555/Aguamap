package cat.copernic.aguamap1.presentation.music

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel puente para exponer el [SoundManager] al grafo de navegación de Compose.
 * Al estar anotado con @HiltViewModel, Hilt se encarga de inyectar el Singleton
 * de SoundManager, permitiendo que la música y los efectos persistan correctamente
 * durante los cambios de configuración (como rotaciones de pantalla).
 */
@HiltViewModel
class SoundManagerViewModel @Inject constructor(
    val soundManager: SoundManager
) : ViewModel() {

    /**
     * El ViewModel actúa como el "propietario" del ciclo de vida del sonido
     * en la capa de presentación. Al estar vinculado a la HomeScreen, la música
     * de fondo puede iniciarse aquí y limpiarse automáticamente cuando
     * el ViewModel se destruya (onCleared).
     */

    override fun onCleared() {
        super.onCleared()
        // Aseguramos que los recursos de audio se liberen al cerrar la app
        soundManager.cleanup()
    }
}