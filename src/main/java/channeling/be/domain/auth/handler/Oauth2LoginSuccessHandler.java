package channeling.be.domain.auth.handler;

import channeling.be.domain.auth.application.MemberOauth2UserService;
import channeling.be.domain.auth.application.MemberOauth2UserService.LoginResult;
import channeling.be.domain.member.application.MemberService;
import channeling.be.global.infrastructure.jwt.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;


@Slf4j
@RequiredArgsConstructor
@Component
public class Oauth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper om;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JwtUtil jwtUtil;    // JWT 토큰 생성기
    private final MemberService memberService;
    private final MemberOauth2UserService memberOauth2UserService;

    // 프론트 콜백
    @Value("${FRONT_URL:http://localhost:5173}")
    private String frontUrl;

    // 로그인 성공 시 처리하는 메서드
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("로그인 성공 후, 헨들러 진입");
        // TODO [지우기] 인증 사용자 정보 샘플 -> 이거는 로그로 남겨둬야할듯..?
        /* -------------------------------------------------
         * 구글 accesstoken 꺼내기
         * ------------------------------------------------- */
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String googleAccessToken = authorizedClientService
                .loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(), // "google"
                        oauthToken.getName())                           // 현재 사용자 식별자
                .getAccessToken()
                .getTokenValue();


        String googleRefreshToken = authorizedClientService
                .loadAuthorizedClient(
                        oauthToken.getAuthorizedClientRegistrationId(), // "google"
                        oauthToken.getName())                           // 현재 사용자 식별자
                .getRefreshToken()
                .getTokenValue();
        System.out.println("Refresh Token: " + googleRefreshToken);




        log.info("컨텍스트에서 구글 엑세스 토큰 추출 = {}", googleAccessToken);
        /* -------------------------------------------------
         * OAuth2User 꺼내기
         * ------------------------------------------------- */
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attrs = oauthUser.getAttributes(); // 멤버 속성
        log.info("컨텍스트에서 토큰 내의 회원 정보 추출 = {}", attrs);


        /* -------------------------------------------------
         * 1. 멤버 생성/조회
         * 2. 채널 생성/조회
         * 3. 유튜브에서 채널정보 조회
         * 4. youtube 조회 -> video id 획득
         * 5. 유튜브에서 비디오 정보 조회
         * ------------------------------------------------- */

        // soft 여부 확인 추가..?
        LoginResult result = memberOauth2UserService.executeGoogleLogin(attrs, googleAccessToken, googleRefreshToken); //



        /*
        서버 자체 토큰 발급
         */
        String accessToken = jwtUtil.createAccessToken(result.member());
        String refreshToken = jwtUtil.createRefreshToken(result.member());

         log.info("Access Token: {}", accessToken);
         log.info("Refresh Token: {}", refreshToken);
        // 프론트 응답 생성
        String targetUrl = UriComponentsBuilder.fromUriString(frontUrl + "/auth/callback") // TODO
                .queryParam("token", accessToken)
                .queryParam("refresh", refreshToken)
                .queryParam("message", "Success")
                .queryParam("channelId", result.channel().getId())
                .queryParam("isNew", result.isNew()) // innter 30, new
                .build()
                .toUriString();

        response.sendRedirect(targetUrl);

    }

}
