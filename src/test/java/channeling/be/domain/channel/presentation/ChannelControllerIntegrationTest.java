package channeling.be.domain.channel.presentation;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.report.domain.Report;
import channeling.be.domain.report.domain.repository.ReportRepository;
import channeling.be.domain.video.domain.Video;
import channeling.be.domain.video.domain.VideoType;
import channeling.be.domain.video.domain.repository.VideoRepository;
import channeling.be.global.annotation.WithMockJwtUser;
import channeling.be.infrastructure.jwt.JwtUtil;
import channeling.be.response.code.status.ErrorStatus;
import channeling.be.response.exception.handler.ChannelHandler;
import channeling.be.support.IntegrationTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;

import static channeling.be.domain.channel.presentation.dto.request.ChannelRequestDto.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ChannelController 통합 테스트
 *
 * ReportControllerTest를 참고하여 작성된 통합 테스트입니다.
 * @WithMockJwtUser를 사용하여 인증된 사용자 환경을 시뮬레이션하고,
 * TestDataFactory를 통해 테스트 데이터를 생성합니다.
 */
@Slf4j
@AutoConfigureMockMvc
@Transactional  // ⭐ 이것만 추가!
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("ChannelController 통합 테스트")
public class ChannelControllerIntegrationTest extends IntegrationTestSupport {

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

    @Autowired
    private ReportRepository reportRepository;


    @Nested
    @DisplayName("GET /channels/{channel-id}/videos - 채널 비디오 목록 조회")
    class GetChannelVideos {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 채널의 SHORT 타입 비디오 목록을 정상적으로 조회한다")
        void getChannelVideos_ShortType_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);

            // 비디오가 있는지 확인
            assertThat(videoList).isNotEmpty();

            VideoType videoType = VideoType.SHORTS;
            int page = 1;
            int size = 8;

            // when
            ResultActions result = mockMvc.perform(
                    get("/channels/{channel-id}/videos", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .param("type", videoType.name())
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.channelId").value(testChannel.getId()))
                    .andExpect(jsonPath("$.result.page").value(page))
                    .andExpect(jsonPath("$.result.size").value(size))
                    .andExpect(jsonPath("$.result.videoList").isArray())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 채널의 LONG 타입 비디오 목록을 정상적으로 조회한다")
        void getChannelVideos_LongType_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));

            VideoType videoType = VideoType.LONG;
            int page = 1;
            int size = 8;

            // when
            ResultActions result = mockMvc.perform(
                    get("/channels/{channel-id}/videos", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .param("type", videoType.name())
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.channelId").value(testChannel.getId()))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 비디오 목록 조회를 시도하면 401 에러가 발생한다")
        void getChannelVideos_WithoutAuth_Fail() throws Exception {
            // given
            Long channelId = 1L;
            VideoType videoType = VideoType.SHORTS;

            // when
            ResultActions result = mockMvc.perform(
                    get("/channels/{channel-id}/videos", channelId)
                            .param("type", videoType.name())
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 기본 페이지 파라미터로 비디오를 조회한다")
        void getChannelVideos_WithDefaultParams(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            VideoType videoType = VideoType.SHORTS;

            // when
            ResultActions result = mockMvc.perform(
                    get("/channels/{channel-id}/videos", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .param("type", videoType.name())
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /channels/{channel-id}/reports - 채널 레포트 목록 조회")
    class GetReports {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 채널의 레포트 목록을 정상적으로 조회한다")
        void getReports_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));

            // 테스트용 레포트 데이터 생성
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            if (!videoList.isEmpty()) {
                Video testVideo = videoList.get(0);
                Report testReport = Report.builder()
                        .video(testVideo)
                        .trends(Collections.emptyList())
                        .comments(Collections.emptyList())
                        .task(null)
                        .title("테스트 레포트")
                        .summary("테스트 요약")
                        .build();
                reportRepository.save(testReport);
            }

            VideoType videoType = VideoType.SHORTS;
            int page = 1;
            int size = 8;

            // when
            ResultActions result = mockMvc.perform(
                    get("/channels/{channel-id}/reports", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .param("type", videoType.name())
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.channelId").value(testChannel.getId()))
                    .andExpect(jsonPath("$.result.reportList").isArray())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 레포트 조회를 시도하면 401 에러가 발생한다")
        void getReports_WithoutAuth_Fail() throws Exception {
            // given
            Long channelId = 1L;
            VideoType videoType = VideoType.SHORTS;

            // when
            ResultActions result = mockMvc.perform(
                    get("/channels/{channel-id}/reports", channelId)
                            .param("type", videoType.name())
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("PATCH /channels/{channel-id}/concepts - 채널 컨셉 수정")
    class EditChannelConcept {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 채널 컨셉을 정상적으로 수정한다")
        void editChannelConcept_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));

            EditChannelConceptReqDto request = new EditChannelConceptReqDto();
            request.setConcept("새로운 채널 컨셉입니다.");
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/channels/{channel-id}/concepts", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.channelId").value(testChannel.getId()))
                    .andExpect(jsonPath("$.result.updatedConcept").value("새로운 채널 컨셉입니다."))
                    .andDo(print());

            // DB에서 변경 확인
            Channel updatedChannel = channelRepository.findById(testChannel.getId())
                    .orElseThrow();
            assertThat(updatedChannel.getConcept()).isEqualTo("새로운 채널 컨셉입니다.");
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: null 컨셉으로 수정 시도 시 400 에러가 발생한다")
        void editChannelConcept_WithNullConcept_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));

            // null 컨셉을 가진 요청
            String requestBody = "{\"concept\": null}";

            // when
            ResultActions result = mockMvc.perform(
                    patch("/channels/{channel-id}/concepts", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: 500자를 초과하는 컨셉으로 수정 시도 시 400 에러가 발생한다")
        void editChannelConcept_WithTooLongConcept_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));

            EditChannelConceptReqDto request = new EditChannelConceptReqDto();
            request.setConcept("a".repeat(501)); // 501자
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/channels/{channel-id}/concepts", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 컨셉 수정을 시도하면 401 에러가 발생한다")
        void editChannelConcept_WithoutAuth_Fail() throws Exception {
            // given
            Long channelId = 1L;
            EditChannelConceptReqDto request = new EditChannelConceptReqDto();
            request.setConcept("새로운 컨셉");
            String requestBody = objectMapper.writeValueAsString(request);

            // when
            ResultActions result = mockMvc.perform(
                    patch("/channels/{channel-id}/concepts", channelId)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("PATCH /channels/{channel-id}/targets - 채널 타겟 수정")
    class EditChannelTarget {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 채널 타겟을 정상적으로 수정한다")
        void editChannelTarget_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));

            EditChannelTargetReqDto request = new EditChannelTargetReqDto();
            // Reflection을 사용하여 private 필드 설정 (또는 생성자/정적 팩토리 메서드 사용)
            String requestBody = "{\"target\": \"새로운 타겟 고객\"}";

            // when
            ResultActions result = mockMvc.perform(
                    patch("/channels/{channel-id}/targets", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.channelId").value(testChannel.getId()))
                    .andExpect(jsonPath("$.result.updatedTarget").value("새로운 타겟 고객"))
                    .andDo(print());

            // DB에서 변경 확인
            Channel updatedChannel = channelRepository.findById(testChannel.getId())
                    .orElseThrow();
            assertThat(updatedChannel.getTarget()).isEqualTo("새로운 타겟 고객");
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: null 타겟으로 수정 시도 시 400 에러가 발생한다")
        void editChannelTarget_WithNullTarget_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));

            String requestBody = "{\"target\": null}";

            // when
            ResultActions result = mockMvc.perform(
                    patch("/channels/{channel-id}/targets", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: 100자를 초과하는 타겟으로 수정 시도 시 400 에러가 발생한다")
        void editChannelTarget_WithTooLongTarget_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));

            String requestBody = "{\"target\": \"" + "a".repeat(101) + "\"}";

            // when
            ResultActions result = mockMvc.perform(
                    patch("/channels/{channel-id}/targets", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .content(requestBody)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /channels/{channel-id} - 채널 정보 조회")
    class GetChannel {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 채널 정보를 정상적으로 조회한다")
        void getChannel_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));

            // when
            ResultActions result = mockMvc.perform(
                    get("/channels/{channel-id}", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.channelId").value(testChannel.getId()))
                    .andExpect(jsonPath("$.result.name").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 채널 정보 조회를 시도하면 401 에러가 발생한다")
        void getChannel_WithoutAuth_Fail() throws Exception {
            // given
            Long channelId = 1L;

            // when
            ResultActions result = mockMvc.perform(
                    get("/channels/{channel-id}", channelId)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("GET /channels/{channel-id}/recommended-videos - 추천 비디오 조회")
    class GetRecommendedVideos {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 추천 비디오 목록을 정상적으로 조회한다")
        void getRecommendedVideos_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));

            int page = 1;
            int size = 10;

            // when
            ResultActions result = mockMvc.perform(
                    get("/channels/{channel-id}/recommended-videos", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size))
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.list").isArray())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 기본 파라미터로 추천 비디오를 조회한다")
        void getRecommendedVideos_WithDefaultParams(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));

            // when
            ResultActions result = mockMvc.perform(
                    get("/channels/{channel-id}/recommended-videos", testChannel.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 추천 비디오 조회를 시도하면 401 에러가 발생한다")
        void getRecommendedVideos_WithoutAuth_Fail() throws Exception {
            // given
            Long channelId = 1L;

            // when
            ResultActions result = mockMvc.perform(
                    get("/channels/{channel-id}/recommended-videos", channelId)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }
    }
}
