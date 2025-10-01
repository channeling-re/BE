package channeling.be.domain.auth.filter;

import channeling.be.domain.auth.application.CustomUserDetailsService;
import channeling.be.global.infrastructure.jwt.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;


/**
 * JWT 인증용 커스텀 필터.
 * - 요청당 한 번(OncePerRequestFilter) 실행된다.
 * - Access Token 검증 → Google ID 추출 → DB 조회 → 인증 객체 생성 과정을 거쳐
 *   SecurityContext에 인증 정보를 저장한다.
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final String[] JWT_WHITELIST = {
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/members/login/**",
            "/dummys/**",
            "/actuator/health",
            "/oauth/**"
    };

    /**
     * 매 요청마다 실행되는 진입점.
     * 토큰을 검증·인증하고, 다음 필터로 체인을 이어준다.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isWhitelisted(request)) {
            log.info("jwt 토큰 인증 제외 URL입니다.");
            // 화이트리스트라면 JWT 인증 로직 수행하지 않고 다음 필터로 진행
            filterChain.doFilter(request, response);
            return;
        }
        checkAccessTokenAndAuthentication(request, response, filterChain);
    }
    private boolean isWhitelisted(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String pattern : JWT_WHITELIST) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
    /**
     * ① 요청 헤더에서 토큰 추출
     * ② 토큰 유효성·블랙리스트 여부 검사
     * ③ 토큰에서 Google ID 추출
     * ④ Google ID로 DB 조회하여 UserDetails 확보
     * ⑤ 인증 정보(SecurityContext) 저장
     */
    private void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        jwtUtil.extractAccessToken(request)
                .filter(jwtUtil::isTokenValid) // 토큰을 검증
                .filter(accessToken -> !jwtUtil.isTokenInBlackList(accessToken)) // 블랙리스틑 여부 판단
                .flatMap(jwtUtil::extractGoogleId) // 토큰에서 구글 아이디 추출
                .map(customUserDetailsService::loadUserByUsername) // db 에서 존재하는 지 확인
                .ifPresent(this::saveAuthentication); // -> 통과하면 로그인 성공! -> 세션에 로그인 멤버 저장
        filterChain.doFilter(request, response);
    }

    /**
     * 인증 객체를 SecurityContextHolder에 저장한다.
     * @param userDetails 인증에 성공한 사용자 정보
     */
    private void saveAuthentication(UserDetails userDetails) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, Collections.emptyList());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }
}
