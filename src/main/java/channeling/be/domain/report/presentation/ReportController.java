package channeling.be.domain.report.presentation;

import channeling.be.global.auth.annotation.LoginMember;
import channeling.be.domain.comment.domain.CommentType;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.report.application.ReportService;
import channeling.be.domain.report.domain.PageType;
import channeling.be.domain.report.domain.Report;
import channeling.be.domain.video.application.VideoService;
import channeling.be.domain.video.domain.Video;
import channeling.be.response.exception.handler.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
public class ReportController implements ReportSwagger {

    private final ReportService reportService;
    private final VideoService videoService;
    @Override
    @GetMapping("/{report-id}/status")
    public ApiResponse<ReportResDto.getReportAnalysisStatus> getReportAnalysisStatus(
            @PathVariable("report-id") Long reportId,
            @LoginMember Member member) {
        return ApiResponse.onSuccess(reportService.getReportAnalysisStatus(member, reportId));
    }

    @Override
    @GetMapping("/{report-id}/comments")
    public ApiResponse<ReportResDto.getCommentsByType> getCommentsByType(
            @PathVariable("report-id") Long reportId,
            @RequestParam(value = "type") CommentType commentType,
            @LoginMember Member member) {
        Report report = reportService.getReportByIdAndMember(reportId, member);
        return ApiResponse.onSuccess(reportService.getCommentsByType(report, commentType));

    }

    @Override
    @GetMapping("/{report-id}/overviews")
    public ApiResponse<ReportResDto.OverviewReport> getReportOverview(
            @PathVariable("report-id") Long reportId,
            @LoginMember Member loginMember) {
        Report report = reportService.checkReport(reportId, PageType.OVERVIEW, loginMember);
        return ApiResponse.onSuccess(ReportConverter.toOverview(report));
    }

    @Override
    @GetMapping("/{report-id}/analyses")
    public ApiResponse<ReportResDto.AnalysisReport> getReportAnalysis(
            @PathVariable("report-id") Long reportId,
            @LoginMember Member loginMember) {
        Report report = reportService.checkReport(reportId, PageType.ANALYSIS, loginMember);
        return ApiResponse.onSuccess(ReportConverter.toAnalysis(report));
    }

    @Override
    @GetMapping("/{report-id}/ideas")
    public ApiResponse<ReportResDto.IdeaReport> getReportIdea(
            @PathVariable("report-id") Long reportId,
            @LoginMember Member loginMember) {
        Report report = reportService.checkReport(reportId, PageType.IDEA, loginMember);
        return ApiResponse.onSuccess(ReportConverter.toIdea(report));
    }

    @Override
    @PostMapping("/{video-id}")
    public ApiResponse<ReportResDto.createReport> createReport(
            @PathVariable("video-id") Long videoId,
            @LoginMember Member member) {
        return ApiResponse.onSuccess(reportService.createReport(member, videoId));
    }

    @Override
    @DeleteMapping("/{report-id}")
    public ApiResponse<ReportResDto.deleteReport> deleteReport(
            @PathVariable("report-id") Long reportId,
            @LoginMember Member member) {
        return ApiResponse.onSuccess(reportService.deleteReport(member, reportId));
    }

    @PostMapping("")
    public ApiResponse<ReportResDto.createReport> createReportByUrl(
            @Valid @RequestBody ReportReqDto.createReportByUrl request,
            @LoginMember Member member) {
        Video video = videoService.checkVideoUrlWithMember(member, request.url());

        return ApiResponse.onSuccess(reportService.createReport(member, video.getId()));
    }
}
