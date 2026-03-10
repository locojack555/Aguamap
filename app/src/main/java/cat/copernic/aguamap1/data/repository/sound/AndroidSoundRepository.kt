package cat.copernic.aguamap1.data.repository.sound

import android.content.Context
import android.media.MediaPlayer
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.sound.SoundType
import cat.copernic.aguamap1.domain.repository.sound.SoundRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementación del repositorio de sonidos para Android utilizando MediaPlayer.
 * Esta clase gestiona tanto la música de fondo persistente como los efectos de sonido puntuales,
 * controlando las transiciones entre ellos (atenuación o pausa de la música al sonar un efecto).
 */
@Singleton
class AndroidSoundRepository @Inject constructor(
    @ApplicationContext private val context: Context // Contexto de la aplicación para acceder a los recursos raw
) : SoundRepository {

    // Reproductor principal para la música de fondo (loop)
    private var backgroundMusicPlayer: MediaPlayer? = null

    // Reproductor secundario para efectos de sonido (ganar/perder)
    private var effectPlayer: MediaPlayer? = null

    // Bandera para recordar si debemos reanudar la música tras terminar un efecto
    private var isBackgroundPausedForEffect = false

    /**
     * Inicia la reproducción de la música de fondo.
     * Si el reproductor no existe, lo crea, lo configura en bucle y ajusta un volumen bajo (30%).
     */
    override fun playBackgroundMusic() {
        if (backgroundMusicPlayer == null) {
            backgroundMusicPlayer = MediaPlayer.create(context, R.raw.background_music).apply {
                isLooping = true // La música de fondo se repite infinitamente
                setVolume(0.3f, 0.3f) // Volumen ambiental
            }
        }

        if (!backgroundMusicPlayer!!.isPlaying) {
            backgroundMusicPlayer?.start()
            isBackgroundPausedForEffect = false
        }
    }

    /**
     * Detiene la música de fondo y libera los recursos de memoria del reproductor.
     */
    override fun stopBackgroundMusic() {
        backgroundMusicPlayer?.stop()
        backgroundMusicPlayer?.release()
        backgroundMusicPlayer = null
        isBackgroundPausedForEffect = false
    }

    /**
     * Verifica si la música de fondo está sonando actualmente.
     */
    override fun isBackgroundMusicPlaying(): Boolean {
        return backgroundMusicPlayer?.isPlaying == true
    }

    /**
     * Reproduce un efecto de sonido específico basado en el tipo proporcionado.
     * Gestiona automáticamente la pausa de la música de fondo para que el efecto se escuche con claridad.
     */
    override fun playSound(soundType: SoundType) {
        // Mapeo del tipo de sonido al recurso de audio correspondiente
        val soundRes = when (soundType) {
            SoundType.GAME_WIN -> R.raw.game_win
            SoundType.GAME_LOSS -> R.raw.game_loss
            else -> return // Si el tipo no está mapeado, no hace nada
        }

        // Si la música de fondo está sonando, la pausamos temporalmente
        val wasBackgroundPlaying = backgroundMusicPlayer?.isPlaying == true
        if (wasBackgroundPlaying) {
            backgroundMusicPlayer?.pause()
            isBackgroundPausedForEffect = true
        }

        // Liberamos cualquier efecto previo que pudiera estar cargado
        effectPlayer?.release()

        // Creamos el nuevo reproductor para el efecto
        effectPlayer = MediaPlayer.create(context, soundRes).apply {
            setVolume(0.5f, 0.5f) // Volumen ligeramente más alto que el fondo
            setOnCompletionListener {
                // Al terminar el sonido, liberamos recursos
                it.release()
                effectPlayer = null

                // Si habíamos pausado la música de fondo, la reanudamos automáticamente
                if (wasBackgroundPlaying) {
                    backgroundMusicPlayer?.start()
                    isBackgroundPausedForEffect = false
                }
            }
            start()
        }
    }

    /**
     * Libera todos los recursos de audio de la aplicación.
     * Se utiliza para limpieza profunda de memoria.
     */
    override fun release() {
        backgroundMusicPlayer?.release()
        effectPlayer?.release()
        backgroundMusicPlayer = null
        effectPlayer = null
        isBackgroundPausedForEffect = false
    }

    /**
     * Detiene inmediatamente cualquier sonido (fondo y efectos) y libera los reproductores.
     */
    override fun stopAllSounds() {
        backgroundMusicPlayer?.stop()
        backgroundMusicPlayer?.release()
        backgroundMusicPlayer = null

        effectPlayer?.stop()
        effectPlayer?.release()
        effectPlayer = null

        isBackgroundPausedForEffect = false
    }
}