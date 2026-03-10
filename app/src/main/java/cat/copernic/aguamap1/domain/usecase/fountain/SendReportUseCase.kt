package cat.copernic.aguamap1.domain.usecase.fountain

import cat.copernic.aguamap1.domain.model.fountain.Report
import cat.copernic.aguamap1.domain.repository.fountain.ReportRepository
import javax.inject.Inject

/**
 * Caso de Uso encargado de tramitar y enviar los reportes de los usuarios.
 * Facilita la comunicación de incidencias, errores en los datos o denuncias
 * de contenido inapropiado hacia los sistemas de gestión de la aplicación.
 *
 * @property repository Repositorio de reportes que gestiona la persistencia en el servidor.
 */
class SendReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    /**
     * Ejecuta el envío de un nuevo reporte a la base de datos.
     * * @param report Objeto [cat.copernic.aguamap1.domain.model.fountain.Report] que contiene el tipo de incidencia, descripción,
     * ID del emisor y, opcionalmente, la referencia a la fuente afectada.
     * @return [Result] que indica si el reporte se ha enviado correctamente.
     */
    suspend operator fun invoke(report: Report) = repository.sendReport(report)
}