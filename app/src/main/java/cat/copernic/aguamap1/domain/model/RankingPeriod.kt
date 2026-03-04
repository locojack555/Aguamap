package cat.copernic.aguamap1.domain.model

/**
 * Define los periodos temporales disponibles para el sistema de clasificaciones (Ranking).
 * Se utiliza para filtrar las consultas a la base de datos y mostrar el rendimiento
 * de los usuarios en diferentes escalas de tiempo.
 */
enum class RankingPeriod {
    /** * Representa la clasificación de las últimas 24 horas (desde las 00:00h).
     * Ideal para fomentar la competitividad diaria inmediata.
     */
    DAY,

    /** * Representa el ranking acumulado del mes natural en curso.
     * Premia la constancia del usuario a lo largo de las semanas.
     */
    MONTH,

    /** * Representa el ranking total acumulado durante el año actual.
     * Muestra a los usuarios más activos y precisos de la temporada completa.
     */
    YEAR
}