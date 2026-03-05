package cat.copernic.aguamap1.data.repository

import android.util.Log
import cat.copernic.aguamap1.domain.model.Report
import cat.copernic.aguamap1.domain.repository.ReportRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * Implementación del repositorio de reportes utilizando Firebase Firestore.
 * Gestiona la creación de denuncias por parte de los usuarios y la consulta/resolución
 * de las mismas por parte de los administradores.
 */
class FirebaseReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore // Instancia de Firestore para interactuar con la colección de reportes
) : ReportRepository {

    /**
     * Envía un nuevo reporte a la base de datos.
     * Genera un ID automático de documento y lo asigna al objeto Report antes de guardarlo.
     * @param report Objeto que contiene la información del reporte (motivo, id de fuente/comentario, etc.).
     * @return Result.success si se guardó correctamente, Result.failure en caso de error.
     */
    override suspend fun sendReport(report: Report): Result<Unit> = try {
        // Obtenemos una referencia a un nuevo documento para generar el ID único
        val docRef = firestore.collection("reports").document()
        val finalReport = report.copy(id = docRef.id)
        // Guardamos el reporte en la colección "reports"
        docRef.set(finalReport).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Recupera todos los reportes que aún no han sido gestionados por un administrador.
     * Filtra los documentos de la colección donde el campo "resolved" es falso.
     * @return Result con la lista de reportes pendientes.
     */
    override suspend fun getPendingReports(): Result<List<Report>> = try {
        val snapshot = firestore.collection("reports")
            .whereEqualTo("resolved", false)
            .orderBy(
                "timestamp",
                Query.Direction.ASCENDING
            )// Filtro para obtener solo los no resueltos
            .get().await()
        // Convertimos los documentos directamente a una lista de objetos de tipo Report
        val reports = snapshot.toObjects(Report::class.java)
        Result.success(reports)
    } catch (e: Exception) {
        Log.e("Reportes", "Error al obtener reportes ordenados: ${e.message}")
        Result.failure(e)
    }

    /**
     * Marca un reporte como resuelto en la base de datos.
     * Cambia el estado del campo "resolved" a true para que deje de aparecer en la lista de pendientes.
     * @param reportId El identificador único del reporte a actualizar.
     */
    override suspend fun resolveReport(reportId: String): Result<Unit> = try {
        // Actualización parcial del documento para modificar únicamente el estado de resolución
        firestore.collection("reports").document(reportId)
            .update("resolved", true).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}