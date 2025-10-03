package channeling.be.global.auth.handler;

import channeling.be.global.infrastructure.jwt.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class Oauth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {


        log.error("로그인 실패");
        exception.getStackTrace();
        // 프론트 응답 생성
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/auth/callback")
                .queryParam("token", "")
                .queryParam("message", exception.getMessage())
                .build()
                .toUriString();


        response.sendRedirect(targetUrl);

    }
}
