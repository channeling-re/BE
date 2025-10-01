package channeling.be.domain.dummy.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import channeling.be.domain.comment.domain.CommentType;
import channeling.be.domain.report.presentation.ReportResDto;
import channeling.be.response.exception.handler.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "더미레포트 관련 API", description = "더미레포트 관련 API입니다. id에 1 혹은 2 를 넣어주세요")
public interface DummySwagger {


    @GetMapping("/{report-id}/comments")
    ApiResponse<ReportResDto.getCommentsByType> getCommentsByType(
        @PathVariable("report-id") Long reportId,
        @RequestParam(value = "type") CommentType commentType);

    @Operation(summary = "리포트 개요 페이지 조회", description = "요청한 리포트의 개요 정보를 조회합니다.\n" +
        "응답 필드의 상세정보는 아래 [ Shemas-OverviewReport ]를 참고해주세요. (ctrl + f)")
    @GetMapping("/{report-id}/overviews")
    ApiResponse<ReportResDto.OverviewReport> getReportOverview(
        @Parameter(description = "요청 리포트 아이디", example = "1")
        @PathVariable("report-id") Long reportId);

    @Operation(summary = "리포트 분석 페이지 조회", description = "요청한 리포트의 분석 정보를 조회합니다.\n" +
        "응답 필드의 상세정보는 아래 [ Shemas-AnalysisReport ]를 참고해주세요. (ctrl + f)")
    @GetMapping("/{report-id}/analyses")
    ApiResponse<ReportResDto.AnalysisReport> getReportAnalysis(
        @Parameter(description = "요청 리포트 아이디", example = "1")
        @PathVariable("report-id") Long reportId);

    @Operation(summary = "리포트 아이디어 페이지 조회", description = "요청한 리포트의 아이디어 정보를 조회합니다.\n" +
        "응답 필드의 상세정보는 아래 [ Shemas-IdeaReport ]를 참고해주세요. (ctrl + f)")
    @GetMapping("/{report-id}/ideas")
    ApiResponse<ReportResDto.IdeaReport> getReportIdea(
        @Parameter(description = "요청 리포트 아이디", example = "1")
        @PathVariable("report-id") Long reportId);
}
