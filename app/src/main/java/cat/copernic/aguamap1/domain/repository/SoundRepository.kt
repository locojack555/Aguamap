package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.SoundType

interface SoundRepository {
    fun playSound(soundType: SoundType)
    fun playBackgroundMusic()
    fun stopBackgroundMusic()
    fun pauseBackgroundMusic()
    fun resumeBackgroundMusic()
    fun release()
    fun isBackgroundMusicPlaying(): Boolean

    fun stopAllSounds()
}