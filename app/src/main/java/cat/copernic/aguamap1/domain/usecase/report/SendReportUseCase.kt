package cat.copernic.aguamap1.domain.usecase.report

import cat.copernic.aguamap1.domain.model.Report
import cat.copernic.aguamap1.domain.repository.ReportRepository
import javax.inject.Inject

class SendReportUseCase @Inject constructor(
    private val repository: ReportRepository
) {
    suspend operator fun invoke(report: Report) = repository.sendReport(report)
}