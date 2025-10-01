package channeling.be.global.infrastructure.jwt;

import channeling.be.domain.member.domain.Member;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Optional;

/**
 * JWT 관련 유틸리티 기능을 정의하는 인터페이스.
 */
public interface JwtUtil {
    /**
     * 회원 정보를 기반으로 액세스 토큰을 생성합니다.
     * @param member 토큰 생성 대상 회원 정보
     * @return 생성된 액세스 토큰 문자열
     */
    String createAccessToken(Member member);
    /**
     * 회원 정보를 기반으로 리프레시 토큰을 생성합니다.
     * @param member 토큰 생성 대상 회원 정보
     * @return 생성된 액세스 토큰 문자열
     */
    String createRefreshToken(Member member);

    /**
     * 주어진 회원 정보와 리프레시 토큰을 검증한 뒤,
     * 새로운 액세스 토큰을 재발급합니다.
     *
     * @param refreshToken  유효성을 검증할 리프레시 토큰
     * @return 재발급된 액세스 토큰 문자열
     */
    String reissueAccessToken(String refreshToken);


    /**
     * HTTP 요청 헤더에서 액세스 토큰을 추출합니다.
     * @param request HTTP 요청 객체
     * @return 액세스 토큰이 존재하면 Optional에 담아 반환, 없으면 빈 Optional
     */
    Optional<String> extractAccessToken(HttpServletRequest request);

    /**
     * 액세스 토큰에서 회원 ID(주로 userId)를 추출합니다.
     * @param accessToken 액세스 토큰 문자열
     * @return 회원 ID가 존재하면 Optional에 담아 반환, 없으면 빈 Optional
     */
    Optional<String> extractGoogleId(String accessToken);

    Optional<Long> extractMemberId(String token);

    /**
     * HTTP 응답 헤더에 액세스 토큰을 설정합니다.
     * @param response HTTP 응답 객체
     * @param accessToken 액세스 토큰 문자열
     */
    void setAccessTokenHeader(HttpServletResponse response, String accessToken);

    /**
     * 토큰의 유효성을 검사합니다.
     * (서명 검증, 만료 여부 등)
     * @param token 검사 대상 토큰 문자열
     * @return 유효하면 true, 아니면 false
     */
    boolean isTokenValid(String token);

    /**
     * 해당 액세스 토큰이 블랙리스트에 포함되어 있는지 검사합니다.
     * (예: 로그아웃 등으로 무효화된 토큰 확인)
     * @param accessToken 검사 대상 액세스 토큰 문자열
     * @return 블랙리스트에 있으면 true, 아니면 false
     */
    boolean isTokenInBlackList(String accessToken);
}