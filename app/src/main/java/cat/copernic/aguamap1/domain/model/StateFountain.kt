package cat.copernic.aguamap1.domain.model

/**
 * Define los estados posibles en el flujo de moderación de una fuente.
 * Este enumerado permite filtrar qué fuentes son visibles para el público general
 * y cuáles requieren la intervención de un administrador.
 */
enum class StateFountain {
    /**
     * Estado inicial de una fuente tras ser dada de alta por un usuario.
     * La fuente permanece en una "bandeja de entrada" para que los administradores
     * verifiquen su veracidad y coordenadas antes de publicarla oficialmente.
     */
    PENDING,

    /**
     * Estado que indica que la fuente ha sido revisada y aprobada.
     * Una fuente en estado ACCEPTED es visible en el mapa para todos los usuarios
     * y es apta para ser seleccionada en el mini-juego diario.
     */
    ACCEPTED
}