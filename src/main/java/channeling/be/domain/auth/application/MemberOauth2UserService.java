package channeling.be.domain.auth.application;

import channeling.be.domain.channel.application.ChannelService;
import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.member.application.MemberService;
import channeling.be.domain.member.domain.Member;
import channeling.be.global.infrastructure.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberOauth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberService memberService;
    private final ChannelService channelService;
    private final RedisUtil redisUtil;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 로그인 진행 시 키값 (sub)
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // oauth user 정보
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);
        Map<String, Object> memberAttribute = oAuth2User.getAttributes();

        // TODO [지우기] 인증 사용자 정보 샘플
        System.out.println(" 엑세스 토큰 " + userRequest.getAccessToken().getTokenValue());
        System.out.println(" 유저 정보 샘플 " + memberAttribute);

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                memberAttribute,
                userNameAttributeName);
    }

    @Transactional
    public LoginResult executeGoogleLogin(Map<String, Object> attrs, String googleAccessToken, String googleRefreshToken) {
        MemberResult memberResult = memberService.findOrCreateMember(
            attrs.get("sub").toString(),
            attrs.get("email").toString(),
            attrs.get("name").toString(),
            attrs.get("picture").toString()

        );
        Member member = memberResult.member;
        redisUtil.saveGoogleAccessToken(member.getId(), googleAccessToken);
        redisUtil.saveGoogleRefreshAccessToken(member.getId(), googleRefreshToken);

        Channel channel = channelService.updateOrCreateChannelByMember(member);

        return new LoginResult(member, channel, memberResult.isNew);
    }

    public record LoginResult(Member member, Channel channel, boolean isNew) {}
    public record MemberResult(Member member, boolean isNew) {}

}
