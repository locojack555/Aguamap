package cat.copernic.aguamap1.domain.model

/**
 * Define los niveles de permisos y autoridad dentro de la aplicación.
 * Se utiliza para la lógica de navegación condicional y la protección de
 * rutas de administración en el panel de control.
 */
enum class UserRole {
    /**
     * Rol estándar para todos los usuarios registrados.
     * Permite visualizar el mapa, añadir fuentes (en estado PENDING),
     * realizar comentarios y participar en el mini-juego diario.
     */
    USER,

    /**
     * Rol con privilegios elevados para la gestión de la plataforma.
     * Permite validar fuentes pendientes, resolver reportes de usuarios,
     * moderar comentarios ofensivos y acceder a métricas globales.
     */
    ADMIN
}