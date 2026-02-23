package cat.copernic.aguamap1.domain.usecase.sound

import cat.copernic.aguamap1.domain.model.SoundType
import cat.copernic.aguamap1.domain.repository.SoundRepository
import javax.inject.Inject

class PlayBackgroundMusicUseCase @Inject constructor(
    private val repository: SoundRepository
) {
    operator fun invoke() = repository.playBackgroundMusic()
}

class StopBackgroundMusicUseCase @Inject constructor(
    private val repository: SoundRepository
) {
    operator fun invoke() = repository.stopBackgroundMusic()
}

class PlayWinSoundUseCase @Inject constructor(
    private val repository: SoundRepository
) {
    operator fun invoke() = repository.playSound(SoundType.GAME_WIN)
}

class PlayLossSoundUseCase @Inject constructor(
    private val repository: SoundRepository
) {
    operator fun invoke() = repository.playSound(SoundType.GAME_LOSS)
}

class StopAllSoundsUseCase @Inject constructor(
    private val repository: SoundRepository
) {
    operator fun invoke() = repository.stopAllSounds()
}
