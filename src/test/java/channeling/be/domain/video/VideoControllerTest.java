package channeling.be.domain.video;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.domain.Video;
import channeling.be.domain.video.domain.repository.VideoRepository;
import channeling.be.global.annotation.WithMockJwtUser;
import channeling.be.infrastructure.jwt.JwtUtil;
import channeling.be.response.code.status.ErrorStatus;
import channeling.be.response.exception.handler.ChannelHandler;
import channeling.be.support.IntegrationTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * VideoController 통합 테스트
 *
 * @Transactional을 사용하여 각 테스트가 자동으로 롤백됩니다.
 */
@Slf4j
@Transactional
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("VideoController 통합 테스트")
public class VideoControllerTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Nested
    @DisplayName("GET /videos/{video-id} - 비디오 정보 조회")
    class GetVideoInfo {

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 비디오 정보를 정상적으로 조회한다")
        void getVideoInfo_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            Video testVideo = videoList.get(0);

            // when
            ResultActions result = mockMvc.perform(
                    get("/videos/{video-id}", testVideo.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.videoId").value(testVideo.getId()))
                    .andExpect(jsonPath("$.result.youtubeVideoId").value(testVideo.getYoutubeVideoId()))
                    .andExpect(jsonPath("$.result.videoTitle").value(testVideo.getTitle()))
                    .andExpect(jsonPath("$.result.videoThumbnailUrl").value(testVideo.getThumbnail()))
                    .andExpect(jsonPath("$.result.videoCategory").value(testVideo.getVideoCategory().name()))
                    .andExpect(jsonPath("$.result.viewCount").value(testVideo.getView()))
                    .andExpect(jsonPath("$.result.videoCreatedDate").exists())
                    .andExpect(jsonPath("$.result.ChannelName").value(testChannel.getName()))
                    .andExpect(jsonPath("$.result.lastUpdatedDate").exists())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("성공: 다른 채널의 비디오 정보를 조회한다")
        void getVideoInfo_OtherChannel_Success(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Channel testChannel = channelRepository.findByMember(member)
                    .orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
            List<Video> videoList = videoRepository.findByChannel(testChannel);
            Video testVideo = videoList.get(0);

            // when
            ResultActions result = mockMvc.perform(
                    get("/videos/{video-id}", testVideo.getId())
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.videoId").exists())
                    .andExpect(jsonPath("$.result.videoTitle").exists())
                    .andExpect(jsonPath("$.result.ChannelName").exists())
                    .andDo(print());
        }

        @Test
        @DisplayName("실패: 인증되지 않은 사용자가 비디오 정보 조회 시 401 에러가 발생한다")
        void getVideoInfo_WithoutAuth_Fail() throws Exception {
            // when
            ResultActions result = mockMvc.perform(
                    get("/videos/{video-id}", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isUnauthorized())
                    .andDo(print());
        }

        @Test
        @WithMockJwtUser
        @DisplayName("실패: 존재하지 않는 비디오 ID로 조회 시 404 에러가 발생한다")
        void getVideoInfo_NotFound_Fail(Member member) throws Exception {
            // given
            String accessToken = jwtUtil.createAccessToken(member);
            Long nonExistentVideoId = 999999L;

            // when
            ResultActions result = mockMvc.perform(
                    get("/videos/{video-id}", nonExistentVideoId)
                            .header("Authorization", "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
            );

            // then
            result.andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }
}