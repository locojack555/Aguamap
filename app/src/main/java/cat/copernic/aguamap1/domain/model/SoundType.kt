package cat.copernic.aguamap1.domain.model

/**
 * Define los diferentes eventos auditivos que pueden ocurrir en la aplicación.
 * Este enumerado actúa como un puente entre la lógica de negocio (dominio)
 * y la implementación del motor de audio (infraestructura).
 */
enum class SoundType {
    /** * Música ambiental que se reproduce de forma continua durante la navegación
     * o la estancia en los menús principales.
     */
    BACKGROUND_MUSIC,

    /** * Efecto de sonido corto y gratificante que se dispara cuando el usuario
     * acierta la ubicación de una fuente en el mini-juego.
     */
    GAME_WIN,

    /** * Efecto de sonido que indica que el usuario no ha logrado superar el
     * desafío diario o ha cometido un error crítico.
     */
    GAME_LOSS
}