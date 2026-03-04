package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.Report

/**
 * Interfaz de Dominio para la gestión de reportes y moderación de contenido.
 * Permite a los usuarios notificar incidencias sobre fuentes o comentarios,
 * y a los administradores gestionar dichas denuncias.
 */
interface ReportRepository {

    /**
     * Envía una nueva denuncia al sistema para su revisión.
     * @param report Objeto con la información del reporte (tipo, ID del recurso, motivo y autor).
     * @return Result.success si se envió correctamente, Result.failure si hubo un error de red.
     */
    suspend fun sendReport(report: Report): Result<Unit>

    /**
     * Recupera la lista de todas las denuncias que aún no han sido gestionadas.
     * Este método es utilizado exclusivamente por perfiles con rol de ADMINISTRADOR.
     * @return Result con la lista de objetos [Report] marcados como no resueltos.
     */
    suspend fun getPendingReports(): Result<List<Report>>

    /**
     * Marca una denuncia específica como gestionada y resuelta.
     * Una vez resuelta, el reporte dejará de aparecer en la lista de pendientes del panel de control.
     * @param reportId Identificador único del reporte a actualizar.
     * @return Result.success al confirmar la actualización en la base de datos.
     */
    suspend fun resolveReport(reportId: String): Result<Unit>
}