package channeling.be.domain.dummy.presentation;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import channeling.be.domain.comment.domain.CommentType;
import channeling.be.domain.report.application.ReportService;
import channeling.be.domain.report.domain.PageType;
import channeling.be.domain.report.domain.Report;
import channeling.be.domain.report.presentation.ReportConverter;
import channeling.be.domain.report.presentation.ReportResDto;
import channeling.be.domain.video.application.VideoService;
import channeling.be.response.code.status.ErrorStatus;
import channeling.be.response.exception.handler.ApiResponse;
import channeling.be.response.exception.handler.ReportHandler;

@RestController
@RequestMapping("/dummys")
public class DummyController implements DummySwagger {
    private final ReportService reportService;
    private final VideoService videoService;

	public DummyController(@Qualifier("dummyReportServiceImpl") ReportService reportService, VideoService videoService) {
		this.reportService = reportService;
		this.videoService = videoService;
	}

	@Override
    @GetMapping("/{report-id}/comments")
    public ApiResponse<ReportResDto.getCommentsByType> getCommentsByType(
        @PathVariable("report-id") Long reportId,
        @RequestParam(value = "type") CommentType commentType) {
        reportId = getDummyReportId(reportId);
        Report report = reportService.getReportByIdAndMember(reportId, null);
        return ApiResponse.onSuccess(reportService.getCommentsByType(report, commentType));

    }


    @Override
    @GetMapping("/{report-id}/overviews")
    public ApiResponse<ReportResDto.OverviewReport> getReportOverview(
        @PathVariable("report-id") Long reportId) {
        reportId = getDummyReportId(reportId);
        Report report = reportService.checkReport(reportId, PageType.OVERVIEW, null);
        return ApiResponse.onSuccess(ReportConverter.toOverview(report));
    }

    @Override
    @GetMapping("/{report-id}/analyses")
    public ApiResponse<ReportResDto.AnalysisReport> getReportAnalysis(
        @PathVariable("report-id") Long reportId) {
        reportId = getDummyReportId(reportId);
        Report report = reportService.checkReport(reportId, PageType.ANALYSIS,null);
        return ApiResponse.onSuccess(ReportConverter.toAnalysis(report));
    }

    @Override
    @GetMapping("/{report-id}/ideas")
    public ApiResponse<ReportResDto.IdeaReport> getReportIdea(
        @PathVariable("report-id") Long reportId) {
        reportId = getDummyReportId(reportId);
        Report report = reportService.checkReport(reportId, PageType.IDEA, null);
        return ApiResponse.onSuccess(ReportConverter.toIdea(report));
    }


    private Long getDummyReportId(Long reportId) {
        if(reportId ==1L) return 28L;
        else if(reportId ==2L) return 29L;
        throw new ReportHandler(ErrorStatus._REPORT_NOT_FOUND);
    }
}
