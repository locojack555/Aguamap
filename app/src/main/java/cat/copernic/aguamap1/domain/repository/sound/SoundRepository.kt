package cat.copernic.aguamap1.domain.repository.sound

import cat.copernic.aguamap1.domain.model.sound.SoundType

/**
 * Interfaz de Dominio para la gestión de efectos de sonido y música de fondo.
 * Define las capacidades multimedia de la aplicación, permitiendo una
 * experiencia inmersiva tanto en el mapa como en los modos de juego.
 */
interface SoundRepository {

    /**
     * Reproduce un efecto de sonido corto basado en una acción del usuario.
     * @param soundType El tipo de sonido a reproducir (ej. SUCCESS, ERROR, CLICK).
     */
    fun playSound(soundType: SoundType)

    /**
     * Inicia la reproducción de la música de fondo de la aplicación.
     * Normalmente se configura para que se reproduzca en bucle (loop)
     * durante la navegación por los menús o el juego.
     */
    fun playBackgroundMusic()

    /**
     * Detiene la música de fondo de forma inmediata.
     */
    fun stopBackgroundMusic()

    /**
     * Libera todos los recursos multimedia utilizados (MediaPlayer, SoundPool).
     * Este método es crítico para evitar fugas de memoria y debe llamarse
     * cuando la aplicación o el servicio de audio se destruyan.
     */
    fun release()

    /**
     * Consulta el estado actual del reproductor de música.
     * @return True si la música de fondo está sonando, False en caso contrario.
     */
    fun isBackgroundMusicPlaying(): Boolean

    /**
     * Detiene todos los sonidos activos, tanto la música de fondo como
     * cualquier efecto que se esté reproduciendo en ese instante.
     */
    fun stopAllSounds()
}