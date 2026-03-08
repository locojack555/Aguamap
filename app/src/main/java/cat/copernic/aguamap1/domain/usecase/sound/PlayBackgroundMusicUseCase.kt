package cat.copernic.aguamap1.domain.usecase.sound

import cat.copernic.aguamap1.domain.model.sound.SoundType
import cat.copernic.aguamap1.domain.repository.sound.SoundRepository
import javax.inject.Inject

/**
 * Caso de Uso para iniciar la reproducción de la música de fondo de la aplicación.
 * Suele activarse al entrar en el menú principal o en la pantalla del juego.
 */
class PlayBackgroundMusicUseCase @Inject constructor(
    private val repository: SoundRepository
) {
    operator fun invoke() = repository.playBackgroundMusic()
}

/**
 * Caso de Uso para detener la música de fondo.
 * Útil cuando el usuario sale de la aplicación o entra en una sección de silencio.
 */
class StopBackgroundMusicUseCase @Inject constructor(
    private val repository: SoundRepository
) {
    operator fun invoke() = repository.stopBackgroundMusic()
}

/**
 * Caso de Uso para reproducir el efecto de sonido de victoria.
 * Se dispara cuando el usuario localiza una fuente con alta precisión en el juego.
 */
class PlayWinSoundUseCase @Inject constructor(
    private val repository: SoundRepository
) {
    operator fun invoke() = repository.playSound(SoundType.GAME_WIN)
}

/**
 * Caso de Uso para reproducir el efecto de sonido de derrota o fallo.
 * Se utiliza cuando el usuario no logra el objetivo del juego diario.
 */
class PlayLossSoundUseCase @Inject constructor(
    private val repository: SoundRepository
) {
    operator fun invoke() = repository.playSound(SoundType.GAME_LOSS)
}

/**
 * Caso de Uso de seguridad para detener cualquier salida de audio de la app.
 * Ideal para gestionar el ciclo de vida (onPause/onStop) o cuando el usuario silencia la app.
 */
class StopAllSoundsUseCase @Inject constructor(
    private val repository: SoundRepository
) {
    operator fun invoke() = repository.stopAllSounds()
}