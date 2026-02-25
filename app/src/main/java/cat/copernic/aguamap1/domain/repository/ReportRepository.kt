package cat.copernic.aguamap1.domain.repository

import cat.copernic.aguamap1.domain.model.Report

interface ReportRepository {
    suspend fun sendReport(report: Report): Result<Unit>
    suspend fun getPendingReports(): Result<List<Report>>
    suspend fun resolveReport(reportId: String): Result<Unit>
}