package channeling.be.domain.report;


import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
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
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(WireMockFastApiTestSupport.class)
public class ReportControllerTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtUtil jwtUtil; // JWT 생성을 위해 JwtUtil 주입
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private VideoRepository videoRepository;
    @MockitoBean
    private ReportRepository reportRepository;

    @Test
    @WithMockJwtUser
    public void createReportTest(Member member) throws Exception {

        //given
        // 테스트 유저 정보로 실제 액세스 토큰 생성
        String accessToken = jwtUtil.createAccessToken(member);
        Channel testChannel = channelRepository.findByMember(member).orElseThrow(() -> new ChannelHandler(ErrorStatus._CHANNEL_NOT_FOUND));
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



}


