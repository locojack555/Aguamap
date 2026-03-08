package cat.copernic.aguamap1.aplication.sound

import cat.copernic.aguamap1.domain.usecase.sound.PlayBackgroundMusicUseCase
import cat.copernic.aguamap1.domain.usecase.sound.PlayLossSoundUseCase
import cat.copernic.aguamap1.domain.usecase.sound.PlayWinSoundUseCase
import cat.copernic.aguamap1.domain.usecase.sound.StopAllSoundsUseCase
import cat.copernic.aguamap1.domain.usecase.sound.StopBackgroundMusicUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor de audio centralizado de la aplicación.
 * Utiliza el patrón Singleton para asegurar que solo exista una instancia controlando
 * los flujos de audio, evitando solapamientos accidentales y fugas de memoria.
 * * Se comunica con la capa de dominio a través de Casos de Uso (Use Cases) para
 * mantener una arquitectura limpia.
 */
@Singleton
class SoundManager @Inject constructor(
    private val playBackgroundMusicUseCase: PlayBackgroundMusicUseCase,
    private val stopBackgroundMusicUseCase: StopBackgroundMusicUseCase,
    private val playWinSoundUseCase: PlayWinSoundUseCase,
    private val playLossSoundUseCase: PlayLossSoundUseCase,
    private val stopAllSoundsUseCase: StopAllSoundsUseCase
) {

    /**
     * Inicia la reproducción de la música ambiental (loop).
     */
    fun startBackgroundMusic() = playBackgroundMusicUseCase()

    /**
     * Detiene específicamente la música de fondo.
     */
    fun stopBackgroundMusic() = stopBackgroundMusicUseCase()

    /**
     * Corta cualquier audio en reproducción (música y efectos).
     */
    fun stopAllSounds() = stopAllSoundsUseCase()


    /**
     * Dispara el efecto sonoro de victoria (ej: al encontrar una fuente o subir de nivel).
     * Nota: La lógica de pausar la música de fondo para priorizar el efecto
     * se gestiona internamente en el repositorio de audio.
     */
    fun playWinSound() {
        playWinSoundUseCase()
    }

    /**
     * Dispara el efecto sonoro de error o derrota.
     */
    fun playLossSound() {
        playLossSoundUseCase()
    }

    /**
     * Método de limpieza para liberar recursos de audio cuando la aplicación
     * entra en segundo plano o se cierra.
     */
    fun cleanup() {
        stopBackgroundMusic()
    }
}