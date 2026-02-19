package cat.copernic.aguamap1.presentation.music

import cat.copernic.aguamap1.domain.usecase.sound.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(
    private val playBackgroundMusicUseCase: PlayBackgroundMusicUseCase,
    private val stopBackgroundMusicUseCase: StopBackgroundMusicUseCase,
    private val playWinSoundUseCase: PlayWinSoundUseCase,
    private val playLossSoundUseCase: PlayLossSoundUseCase,
    private val pauseBackgroundMusicUseCase: PauseBackgroundMusicUseCase,
    private val resumeBackgroundMusicUseCase: ResumeBackgroundMusicUseCase,
    private val stopAllSoundsUseCase: StopAllSoundsUseCase
) {

    fun startBackgroundMusic() = playBackgroundMusicUseCase()

    fun stopBackgroundMusic() = stopBackgroundMusicUseCase()

    fun stopAllSounds() = stopAllSoundsUseCase()

    fun playWinSound() {
        // El repositorio ya maneja la pausa/reanudación automática
        playWinSoundUseCase()
    }

    fun playLossSound() {
        // El repositorio ya maneja la pausa/reanudación automática
        playLossSoundUseCase()
    }

    fun pauseMusic() = pauseBackgroundMusicUseCase()

    fun resumeMusic() = resumeBackgroundMusicUseCase()

    fun cleanup() {
        stopBackgroundMusic()
    }
}