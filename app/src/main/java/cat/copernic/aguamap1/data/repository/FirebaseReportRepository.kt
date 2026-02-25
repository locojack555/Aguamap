package cat.copernic.aguamap1.data.repository

import cat.copernic.aguamap1.domain.model.Report
import cat.copernic.aguamap1.domain.repository.ReportRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseReportRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReportRepository {

    override suspend fun sendReport(report: Report): Result<Unit> = try {
        val docRef = firestore.collection("reports").document()
        val finalReport = report.copy(id = docRef.id)
        docRef.set(finalReport).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getPendingReports(): Result<List<Report>> = try {
        val snapshot = firestore.collection("reports")
            .whereEqualTo("resolved", false)
            .get().await()
        val reports = snapshot.toObjects(Report::class.java)
        Result.success(reports)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun resolveReport(reportId: String): Result<Unit> = try {
        firestore.collection("reports").document(reportId)
            .update("resolved", true).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}