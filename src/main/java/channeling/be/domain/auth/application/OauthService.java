package channeling.be.domain.auth.application;

import channeling.be.domain.auth.presentation.OauthReq;
import channeling.be.domain.member.application.MemberService;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.member.domain.repository.MemberRepository;
import channeling.be.global.infrastructure.jwt.JwtUtil;
import channeling.be.global.infrastructure.redis.RedisUtil;
import channeling.be.response.code.status.ErrorStatus;
import channeling.be.response.exception.handler.MemberHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OauthService {
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final MemberRepository memberRepository;

    @Transactional
    public String withdrawMember(Member loginMember, OauthReq.withdrawal request) {
        //영속화
        Member member = memberRepository.findById(loginMember.getId()).orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        member.softDelete();

        String accessToken = request.getAccessToken();
        String refreshToken = request.getRefreshToken();
        System.out.println("access : " + accessToken);
        System.out.println("refresh : " + refreshToken);
        //요청으로 넘어 온 토큰 검증
        jwtUtil.isTokenValid(accessToken);;
        jwtUtil.isTokenInBlackList(accessToken);
        jwtUtil.isTokenValid(refreshToken);;
        jwtUtil.isTokenInBlackList(refreshToken);

        redisUtil.deleteGoogleAccessToken(member.getId());
        redisUtil.deleteGoogleRefreshToken(member.getId());

        //입력받은 서버 토큰들 블렉리스트 처리
        redisUtil.addAccessTokenToBlackList(request.getAccessToken());
        redisUtil.addRefreshTokenToBlackList(request.getRefreshToken());

        return "성공적으로 회원 탈퇴 되었습니다.";
    }

    @Transactional
    public String resignMember(OauthReq.resignation request) {
        String tempToken = request.getTempToken();
        jwtUtil.isTokenValid(tempToken);
        jwtUtil.isTokenInBlackList(tempToken);

        Optional<Long> memberId = jwtUtil.extractMemberId(tempToken);
        Member member = memberRepository.findById(memberId.get()).orElseThrow(() -> new MemberHandler(ErrorStatus._MEMBER_NOT_FOUND));
        member.resign();

        return "재회원가입이 완료되었습니다. 다시 구글 로그인 시도해주세요";
    }
}
