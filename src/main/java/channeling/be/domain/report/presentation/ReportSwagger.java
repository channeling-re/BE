package channeling.be.domain.report.presentation;

import channeling.be.global.auth.annotation.LoginMember;
import channeling.be.domain.comment.domain.CommentType;
import channeling.be.domain.member.domain.Member;
import channeling.be.response.exception.handler.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "리포트 API", description = "리포트 관련 API입니다.")
public interface ReportSwagger {

    @Operation(summary = "리포트 분석 상태를 조회합니다.", description = "순서대로 개요, 분석, 아이디어.")
    @GetMapping("{report-id}/status")
    ApiResponse<ReportResDto.getReportAnalysisStatus> getReportAnalysisStatus(
            @Parameter(description = "상태를 조회할 리포트 아이디 (리포트 분석 요청 시, 응답에 포함 되어 있습니다.)", example = "1")
            @PathVariable("report-id") Long reportId,
            @Parameter(hidden = true)
            @LoginMember Member member);

    @Operation(summary = "리포트의 댓글을 카테고리별로 조회합니다.", description = "상위 5개만 조회합니다.")
    @GetMapping("/{report-id}/comments")
    ApiResponse<ReportResDto.getCommentsByType> getCommentsByType(
            @Parameter(description = "댓글을 조회할 리포트 아이디", example = "1")
            @PathVariable("report-id") Long reportId,
            @Parameter(description = "댓글의 타입.")
            @RequestParam("type") CommentType commentType,
            @Parameter(hidden = true)
            @LoginMember Member member);

    @Operation(summary = "리포트 분석을 요청합니다.", description = "영상 아이디를 입력 받습니다.")
    @PostMapping("/{video-id}")
    ApiResponse<ReportResDto.createReport> createReport(
            @Parameter(description = "리포트 분석을 요청할 영상 아이디 (본인 채널의 영상이어야 합니다.)", example = "1")
            @PathVariable("video-id") Long videoId,
            @Parameter(hidden = true)
            @LoginMember Member member);

    @Operation(summary = "리포트 개요 페이지 조회", description = "요청한 리포트의 개요 정보를 조회합니다.\n" +
            "응답 필드의 상세정보는 아래 [ Shemas-OverviewReport ]를 참고해주세요. (ctrl + f)")
    @GetMapping("/{report-id}/overviews")
    ApiResponse<ReportResDto.OverviewReport> getReportOverview(
            @Parameter(description = "요청 리포트 아이디", example = "1")
            @PathVariable("report-id") Long reportId,
            @Parameter(hidden = true) Member loginMember);

    @Operation(summary = "리포트 분석 페이지 조회", description = "요청한 리포트의 분석 정보를 조회합니다.\n" +
            "응답 필드의 상세정보는 아래 [ Shemas-AnalysisReport ]를 참고해주세요. (ctrl + f)")
    @GetMapping("/{report-id}/analyses")
    ApiResponse<ReportResDto.AnalysisReport> getReportAnalysis(
            @Parameter(description = "요청 리포트 아이디", example = "1")
            @PathVariable("report-id") Long reportId,
            @Parameter(hidden = true) Member loginMember);

    @Operation(summary = "리포트 아이디어 페이지 조회", description = "요청한 리포트의 아이디어 정보를 조회합니다.\n" +
            "응답 필드의 상세정보는 아래 [ Shemas-IdeaReport ]를 참고해주세요. (ctrl + f)")
    @GetMapping("/{report-id}/ideas")
    ApiResponse<ReportResDto.IdeaReport> getReportIdea(
            @Parameter(description = "요청 리포트 아이디", example = "1")
            @PathVariable("report-id") Long reportId,
            @Parameter(hidden = true) Member loginMember);

    @Operation(summary = "리포트 삭제", description = "요청한 리포트를 삭제합니다.")
    @DeleteMapping("/{report-id}")
    ApiResponse<ReportResDto.deleteReport> deleteReport(
            @Parameter(description = "요청 리포트 아이디", example = "1")
            @PathVariable("report-id") Long reportId,
            @Parameter(hidden = true)
            @LoginMember Member member);

    @Operation(summary = "url 로 리포트 생성 요청", description = "url 주소로 리포트 생성을 요청합니다.")
    @PostMapping("")
    ApiResponse<ReportResDto.createReport> createReportByUrl(
            @Valid @RequestBody ReportReqDto.createReportByUrl request,
            @Parameter(hidden = true)
            @LoginMember Member member);
}
