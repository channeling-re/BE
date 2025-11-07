package channeling.be.global.auth;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.member.domain.Member;
import channeling.be.global.auth.handler.Oauth2LoginSuccessHandler;
import channeling.be.infrastructure.kafka.consumer.KafkaConsumerService;
import channeling.be.infrastructure.kafka.dto.KafkaVideoSyncDto;
import channeling.be.infrastructure.redis.RedisUtil;
import channeling.be.support.IntegrationTestSupport;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;

@Slf4j
@AutoConfigureMockMvc
@SpringBootTest
public class AuthIntegrationTest extends IntegrationTestSupport {

    // 핸들러를 직접 호출하는 방식
    @Autowired
    private Oauth2LoginSuccessHandler oauth2LoginSuccessHandler;
    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;
    @MockitoBean
    private OAuth2AuthorizedClientService authorizedClientService;

    // 검증
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private ConsumerFactory<String, Object> testConsumerFactory;
    @MockitoBean
    private KafkaConsumerService kafkaConsumerService;


    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig()
                            .dynamicPort()
                            .usingFilesUnderDirectory("src/test/resources")
            )
            .build();

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("youtube-url.channel",
                () -> wm.getRuntimeInfo().getHttpBaseUrl() + "/youtube/v3");
    }

    @DisplayName("신규 사용자가 구글 로그인을 성공하면, 자체 JWT 토큰과 isNew=true 값을 포함하여 리디렉션한다.")
    @Transactional
    @Test
    void newMemberGoogleLoginSuccess2() throws Exception {
        // * 1. given: SuccessHandler 에 전달될 가짜(Mock) 객체 생성 * //

        /**
         * (1) SuccessHandler 파라미터 생성 : authentication
         **/
        // OAuth2 사용자 속성
        Map<String, Object> userAttributes = Map.of(
                "sub", "123456789",
                "name", "테스트유저",
                "email", "test@gmaill.com",
                "picture", "test.jpg"
        );
        OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), userAttributes, "sub");

        // Authentication
        OAuth2AuthenticationToken mockAuthentication = new OAuth2AuthenticationToken(
                oAuth2User,
                Collections.emptyList(),
                "google" // authorizedClientRegistrationId
        );

        /**
         * (2) SuccessHandler 파라미터 생성 : HttpServletRequest / Response
         **/
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockResponse = new MockHttpServletResponse();

        /**
         * (3) SuccessHandler 내부 로직 given
         **/
        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId("google");
        OAuth2AccessToken mockAccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "mock-google-access-token", Instant.now(), Instant.now().plusSeconds(3600));
        OAuth2RefreshToken mockRefreshToken = new OAuth2RefreshToken("mock-google-refresh-token", Instant.now());
        OAuth2AuthorizedClient mockClient = new OAuth2AuthorizedClient(clientRegistration, "123456789", mockAccessToken, mockRefreshToken);

        // SuccessHandler 내부의 authorizedClientService.loadAuthorizedClient()가 호출될 때
        given(authorizedClientService.loadAuthorizedClient(
                eq("google"),
                eq("123456789") // oAuth2User.getName() 값
        )).willReturn(mockClient);

        // SuccessHandler 내부의 memberOauth2UserService.executeGoogleLogin()이 호출될 때
        Member newMember = Member.builder().id(1L).googleId("123456789").build();
        Channel newChannel = Channel.builder().id(1L).member(newMember).build();

        /**
         * (4) 유튜브 채널 가져오기 스텁
         **/
        wm.stubFor(WireMock.get(WireMock.urlPathEqualTo("/youtube/v3/channels"))
                .withQueryParam("part", WireMock.matching(".*snippet.*contentDetails.*statistics.*"))
                .withQueryParam("mine", WireMock.equalTo("true"))
                .withHeader("Authorization", WireMock.containing("Bearer "))
                // 파일 사용(경로: src/test/resources/__files/dummy/login-channel-response.json)
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("dummy/login-channel-response.json")));

        // 컨슈머 동작 무시
        doNothing().when(kafkaConsumerService).consumeVideoSyncRequest(any(KafkaVideoSyncDto.class));

        //* 2. when: MockMvc.perform() 대신, 핸들러의 메서드를 직접 호출합니다. *//
        oauth2LoginSuccessHandler.onAuthenticationSuccess(mockRequest, mockResponse, mockAuthentication);

        //* 3. then: 핸들러의 응답(리디렉션)을 검증합니다. *//
        String redirectedUrl = mockResponse.getRedirectedUrl();
        // java.net.URI를 이용해 파싱
        URI uri = URI.create(redirectedUrl);
        String query = uri.getQuery();
        // java.net.URLDecoder로 디코딩 후 key-value로 분리
        Map<String, String> queryParams = Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .collect(Collectors.toMap(
                        arr -> URLDecoder.decode(arr[0], StandardCharsets.UTF_8),
                        arr -> arr.length > 1 ? URLDecoder.decode(arr[1], StandardCharsets.UTF_8) : ""
                ));
        Channel channel = channelRepository.findById(Long.valueOf(queryParams.get("channelId"))).orElseThrow(() -> new RuntimeException("채널이 존재하지 않습니다."));


        assertThat(redirectedUrl).isNotNull();
        assertThat(redirectedUrl).startsWith("http://localhost:5173/auth/callback");
        // 응답값 확인
        assertThat(redirectedUrl).contains("token=");
        assertThat(redirectedUrl).contains("refresh=");
        assertThat(redirectedUrl).contains("message=Success");
        assertThat(redirectedUrl).contains("channelId=");
        assertThat(redirectedUrl).contains("isNew=true");
        // Redis에 토큰 저장 여부 확인
        assertThat(redisUtil.getData("GOOGLE_AT_"+channel.getMember().getId()).isEmpty()).isFalse();
        assertThat(redisUtil.getData("GOOGLE_RT_"+channel.getMember().getId()).isEmpty()).isFalse();

        // 카프카
        Consumer<String, Object> consumer = testConsumerFactory.createConsumer();
        consumer.subscribe(Collections.singleton("video-sync-topic"));
        Object payload = awaitOneRecordValue(consumer, "video-sync-topic", Duration.ofSeconds(3));

        assertThat(payload).isNotNull();
    }

}

