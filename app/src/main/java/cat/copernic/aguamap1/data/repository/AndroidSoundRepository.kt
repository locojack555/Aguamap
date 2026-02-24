package cat.copernic.aguamap1.data.repository

import android.content.Context
import android.media.MediaPlayer
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.SoundType
import cat.copernic.aguamap1.domain.repository.SoundRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidSoundRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SoundRepository {

    private var backgroundMusicPlayer: MediaPlayer? = null
    private var effectPlayer: MediaPlayer? = null
    private var isBackgroundPausedForEffect = false

    override fun playBackgroundMusic() {
        if (backgroundMusicPlayer == null) {
            backgroundMusicPlayer = MediaPlayer.create(context, R.raw.background_music).apply {
                isLooping = true
                setVolume(0.3f, 0.3f)
            }
        }

        if (!backgroundMusicPlayer!!.isPlaying) {
            backgroundMusicPlayer?.start()
            isBackgroundPausedForEffect = false
        }
    }

    override fun stopBackgroundMusic() {
        backgroundMusicPlayer?.stop()
        backgroundMusicPlayer?.release()
        backgroundMusicPlayer = null
        isBackgroundPausedForEffect = false
    }

    override fun isBackgroundMusicPlaying(): Boolean {
        return backgroundMusicPlayer?.isPlaying == true
    }

    override fun playSound(soundType: SoundType) {
        val soundRes = when (soundType) {
            SoundType.GAME_WIN -> R.raw.game_win
            SoundType.GAME_LOSS -> R.raw.game_loss
            else -> return
        }

        val wasBackgroundPlaying = backgroundMusicPlayer?.isPlaying == true
        if (wasBackgroundPlaying) {
            backgroundMusicPlayer?.pause()
            isBackgroundPausedForEffect = true
        }

        effectPlayer?.release()

        effectPlayer = MediaPlayer.create(context, soundRes).apply {
            setVolume(0.5f, 0.5f)
            setOnCompletionListener {
                it.release()
                effectPlayer = null

                if (wasBackgroundPlaying) {
                    backgroundMusicPlayer?.start()
                    isBackgroundPausedForEffect = false
                }
            }
            start()
        }
    }

    override fun release() {
        backgroundMusicPlayer?.release()
        effectPlayer?.release()
        backgroundMusicPlayer = null
        effectPlayer = null
        isBackgroundPausedForEffect = false
    }

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