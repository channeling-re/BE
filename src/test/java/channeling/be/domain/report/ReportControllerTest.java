package channeling.be.domain.report;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.comment.domain.CommentType;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.report.domain.Report;
import channeling.be.domain.report.domain.repository.ReportRepository;
import channeling.be.domain.video.domain.Video;
import channeling.be.domain.video.domain.repository.VideoRepository;
import channeling.be.global.annotation.WithMockJwtUser;
import channeling.be.infrastructure.jwt.JwtUtil;
import channeling.be.response.code.status.ErrorStatus;
import channeling.be.response.exception.handler.ChannelHandler;
import channeling.be.support.IntegrationTestSupport;
import channeling.be.support.WireMockFastApiTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static channeling.be.domain.report.presentation.ReportReqDto.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ReportController 통합 테스트
 *
 * @Transactional을 사용하여 각 테스트가 자동으로 롤백됩니다.
 * 기존 createReportTest를 유지하면서 모든 Report API를 테스트합니다.
 */
@Slf4j
@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(WireMockFastApiTestSupport.class)
@DisplayName("ReportController 통합 테스트")
public class ReportControllerTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private VideoRepository videoRepository;

    @MockitoBean
    private ReportRepository reportRepository;

    // ========================================
    // 기존 테스트 유지
    // ========================================

    @Test
    @WithMockJwtUser
    @DisplayName("기존 테스트: 레포트 생성")
    public void createReportTest(Member member) throws Exception {
        //given
        // 테스트 유저 정보로 실제 액세스 토큰 생성
        String accessToken = jwtUtil.createAccessToken(member);
        Channel testChannel = channelRepository.findByMember(member)
                .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
        List<Video> videoList = videoRepository.findByChannel(testChannel);
        Video testVideo = videoList.get(0);

        Report report = Report.builder()
                .id(1L)
                .video(testVideo)
                .trends(Collections.emptyList())
                .comments(Collections.emptyList())
                .task(null)
                .title(null)
                .view(null)
                .viewTopicAvg(null)
                .viewChannelAvg(null)
                .likeCount(null)
                .likeTopicAvg(null)
                .likeChannelAvg(null)
                .comment(null)
                .commentTopicAvg(null)
                .commentChannelAvg(null)
                .concept(null)
                .seo(null)
                .revisit(null)
                .summary(null)
                .neutralComment(null)
                .adviceComment(null)
                .positiveComment(null)
                .negativeComment(null)
                .leaveAnalyze(null)
                .optimization(null)
                .build();

        given(reportRepository.findByTaskId(anyLong()))
                .willReturn(Optional.of(report));

        // when
        ResultActions result = mockMvc.perform(
                post("/reports/{videoId}", testVideo.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.result.videoId").value(testVideo.getId()))
                .andExpect(jsonPath("$.result.reportId").exists());
    }

    // ========================================
    // 새로운 테스트 케이스들
    // ========================================

    @Nested
    @DisplayName("POST /reports/{video-id} - 레포트 생성")
    class CreateReport {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 비디오 ID로 레포트를 생성한다")
        void createReport_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            Video testVideo = videoList.get(0);

            Report mockReport = Report.builder()
                    .id(1L)
                    .video(testVideo)
                    .trends(Collections.emptyList())
                    .comments(Collections.emptyList())
                    .build();

            given(reportRepository.findByTaskId(anyLong()))
                    .willReturn(Optional.of(mockReport));

            // when
            ResultActions result = mockMvc.perform(
                    post("/reports/{video-id}", testVideo.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.videoId").value(testVideo.getId()))
                    .andExpect(jsonPath("$.result.reportId").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 레포트 생성 시도 시 401 에러가 발생한다")
        void createReport_WithoutAuth_Fail() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    post("/reports/{video-id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("POST /reports - URL로 레포트 생성")
    class CreateReportByUrl {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 유튜브 URL로 레포트를 생성한다")
        void createReportByUrl_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            Video testVideo = videoList.get(0);

            createReportByUrl request = new createReportByUrl(
                    "https://www.youtube.com/shorts/_EWJ5Q0Ujbs"
            );
            String requestBody = objectMapper.writeValueAsString(request);

            Report mockReport = Report.builder()
                    .id(1L)
                    .video(testVideo)
                    .trends(Collections.emptyList())
                    .comments(Collections.emptyList())
                    .build();

            given(reportRepository.findByTaskId(anyLong()))
                    .willReturn(Optional.of(mockReport));

            // when
            ResultActions result = mockMvc.perform(
                    post("/reports")
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.reportId").exists())
                    .andExpect(jsonPath("$.result.videoId").exists())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: URL이 null인 경우 400 에러가 발생한다")
        void createReportByUrl_NullUrl_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            String requestBody = "{\"url\": null}";

            // when
            ResultActions result = mockMvc.perform(
                    post("/reports")
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 URL로 레포트 생성 시 401 에러가 발생한다")
        void createReportByUrl_WithoutAuth_Fail() throws Exception {
            // given
            createReportByUrl request = new createReportByUrl(
                    "https://www.youtube.com/shorts/_EWJ5Q0Ujbs"
            );
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    post("/reports")
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /reports/{report-id}/status - 레포트 분석 상태 조회")
    class GetReportAnalysisStatus {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 레포트 분석 상태를 정상적으로 조회한다")
        void getReportAnalysisStatus_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Long reportId = 1L;

            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/status", reportId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.reportId").value(reportId))
                    .andExpect(jsonPath("$.result.overviewStatus").exists())
                    .andExpect(jsonPath("$.result.analysisStatus").exists())
                    .andExpect(jsonPath("$.result.ideaStatus").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 상태 조회 시 401 에러가 발생한다")
        void getReportAnalysisStatus_WithoutAuth_Fail() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/status", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /reports/{report-id}/comments - 타입별 댓글 조회")
    class GetCommentsByType {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: POSITIVE 타입 댓글을 정상적으로 조회한다")
        void getCommentsByType_Positive_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Long reportId = 1L;
            CommentType commentType = CommentType.POSITIVE;

            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/comments", reportId)
                            .header("Authorization", "Bearer " + accessToken)
                            .param("type", commentType.name())
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.commentType").value(commentType.name()))
                    .andExpect(jsonPath("$.result.commentList").isArray())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: NEGATIVE 타입 댓글을 정상적으로 조회한다")
        void getCommentsByType_Negative_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Long reportId = 1L;
            CommentType commentType = CommentType.NEGATIVE;

            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/comments", reportId)
                            .header("Authorization", "Bearer " + accessToken)
                            .param("type", commentType.name())
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.commentType").value(commentType.name()))
                    .andExpect(jsonPath("$.result.commentList").isArray())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: NEUTRAL 타입 댓글을 정상적으로 조회한다")
        void getCommentsByType_Neutral_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Long reportId = 1L;

            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/comments", reportId)
                            .header("Authorization", "Bearer " + accessToken)
                            .param("type", CommentType.NEUTRAL.name())
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.commentType").value(CommentType.NEUTRAL.name()))
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: ADVICE_OPINION 타입 댓글을 정상적으로 조회한다")
        void getCommentsByType_AdviceOpinion_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Long reportId = 1L;

            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/comments", reportId)
                            .header("Authorization", "Bearer " + accessToken)
                            .param("type", CommentType.ADVICE_OPINION.name())
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.commentType").value(CommentType.ADVICE_OPINION.name()))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 댓글 조회 시 401 에러가 발생한다")
        void getCommentsByType_WithoutAuth_Fail() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/comments", 1L)
                            .param("type", CommentType.POSITIVE.name())
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /reports/{report-id}/overviews - 레포트 개요 조회")
    class GetReportOverview {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 레포트 개요를 정상적으로 조회한다")
        void getReportOverview_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Long reportId = 1L;

            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/overviews", reportId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.reportId").value(reportId))
                    // 영상 평가 필드
                    .andExpect(jsonPath("$.result.view").exists())
                    .andExpect(jsonPath("$.result.likeCount").exists())
                    .andExpect(jsonPath("$.result.comment").exists())
                    .andExpect(jsonPath("$.result.concept").exists())
                    .andExpect(jsonPath("$.result.seo").exists())
                    .andExpect(jsonPath("$.result.revisit").exists())
                    // 영상 요약
                    .andExpect(jsonPath("$.result.summary").exists())
                    // 댓글 반응
                    .andExpect(jsonPath("$.result.neutralComment").exists())
                    .andExpect(jsonPath("$.result.adviceComment").exists())
                    .andExpect(jsonPath("$.result.positiveComment").exists())
                    .andExpect(jsonPath("$.result.negativeComment").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 개요 조회 시 401 에러가 발생한다")
        void getReportOverview_WithoutAuth_Fail() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/overviews", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /reports/{report-id}/analyses - 레포트 분석 조회")
    class GetReportAnalysis {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 레포트 분석을 정상적으로 조회한다")
        void getReportAnalysis_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Long reportId = 1L;

            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/analyses", reportId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.reportId").value(reportId))
                    .andExpect(jsonPath("$.result.leaveAnalyze").exists())
                    .andExpect(jsonPath("$.result.optimization").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 분석 조회 시 401 에러가 발생한다")
        void getReportAnalysis_WithoutAuth_Fail() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/analyses", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /reports/{report-id}/ideas - 레포트 아이디어 조회")
    class GetReportIdea {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 레포트 아이디어를 정상적으로 조회한다")
        void getReportIdea_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Long reportId = 1L;

            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/ideas", reportId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.reportId").value(reportId))
                    .andExpect(jsonPath("$.result.idea").isArray())
                    .andExpect(jsonPath("$.result.trend").isArray())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 아이디어 조회 시 401 에러가 발생한다")
        void getReportIdea_WithoutAuth_Fail() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    get("/reports/{report-id}/ideas", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("DELETE /reports/{report-id} - 레포트 삭제")
    class DeleteReport {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 레포트를 정상적으로 삭제한다")
        void deleteReport_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Long reportId = 1L;

            // when
            ResultActions result = mockMvc.perform(
                    delete("/reports/{report-id}", reportId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.reportId").value(reportId))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 레포트 삭제 시 401 에러가 발생한다")
        void deleteReport_WithoutAuth_Fail() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    delete("/reports/{report-id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }
}