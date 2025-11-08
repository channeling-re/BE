package channeling.be.domain.channel;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.channel.presentation.dto.request.ChannelRequestDto;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.member.domain.repository.MemberRepository;
import channeling.be.infrastructure.jwt.JwtUtil;
import channeling.be.support.IntegrationTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class ChannelControllerTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ChannelRepository channelRepository;
    @Autowired
    private JwtUtil jwtUtil; // JWT 생성을 위해 JwtUtil 주입

    private Member testUser;
    private Channel testChannel;

    // @WithUserDetails를 사용하기 위해, 테스트 시작 전에 SecurityContext에 들어갈 유저 정보를 미리 DB에 저장합니다.
    @BeforeEach
    void setUp() {
        channelRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();

        testUser = Member.builder()
                .nickname("testuser")
                .googleEmail("test@google.com")
                .googleId("123456789") // UserDetailsService에서 이 값으로 유저를 찾습니다.
                .build();
        memberRepository.save(testUser);

        testChannel = Channel.builder()
                .member(testUser)
                .name("Test Channel")
                .youtubeChannelId("UC-test-channel")
                .youtubePlaylistId("PL-test-playlist")
                .link("http://youtube.com/channel/UC-test-channel")
                .joinDate(LocalDateTime.now())
                .channelUpdateAt(LocalDateTime.now())
                .image("test_image_url")
                .build();
        channelRepository.save(testChannel);
    }

    @DisplayName("인증된 사용자는 자신의 채널 정보를 성공적으로 조회한다.")
    @Test
//    @WithUserDetails(value = "123456789", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void getMyChannelInfoSuccess() throws Exception {
        // 테스트 유저 정보로 실제 액세스 토큰 생성
        String accessToken = jwtUtil.createAccessToken(testUser);

        // when
        ResultActions resultActions = mockMvc.perform(
                get("/channels/" + testChannel.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.result.channelId").value(testChannel.getId()))
                .andExpect(jsonPath("$.result.name").value("Test Channel"))
                .andDo(print());
    }

    @DisplayName("인증되지 않은 사용자가 채널 정보 조회를 시도하면 401 Unauthorized 에러가 발생한다.")
    @Test
    void getChannelInfoFailWithoutAuth() throws Exception {
        // when
        ResultActions resultActions = mockMvc.perform(
                get("/channels/" + testChannel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isUnauthorized())
                .andDo(print());
    }


    @DisplayName("인증된 사용자는 자신의 채널 컨셉을 성공적으로 수정한다.")
    @Test
    @WithUserDetails(value = "123456789", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    void editChannelConceptSuccess() throws Exception {
        // given
        ChannelRequestDto.EditChannelConceptReqDto request = new ChannelRequestDto.EditChannelConceptReqDto();
        request.setConcept("새로운 채널 컨셉입니다.");
        String requestBody = objectMapper.writeValueAsString(request);

        // when
        ResultActions resultActions = mockMvc.perform(
                patch("/channels/" + testChannel.getId() + "/concepts")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.result.channelId").value(testChannel.getId()))
                .andExpect(jsonPath("$.result.updatedConcept").value("새로운 채널 컨셉입니다."))
                .andDo(print());

        Channel updatedChannel = channelRepository.findById(testChannel.getId()).get();
        assertThat(updatedChannel.getConcept()).isEqualTo("새로운 채널 컨셉입니다.");
    }
}
