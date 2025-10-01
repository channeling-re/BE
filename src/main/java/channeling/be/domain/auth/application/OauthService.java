package channeling.be.domain.auth.application;

import channeling.be.global.infrastructure.jwt.JwtUtil;
import channeling.be.global.infrastructure.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OauthService {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    public String logout(String refreshToken) {
        jwtUtil.isTokenValid(refreshToken); //리프레시 토큰 검증
        jwtUtil.isTokenInBlackList(refreshToken); //로그아웃된 사람인지 검증
        redisUtil.addRefreshTokenToBlackList(refreshToken); // 블렉리스트에 넣기
        return "성공적으로 로그아웃하였습니다.";
    }
}
