package channeling.be.global.annotation;

import channeling.be.domain.member.domain.Member;
import channeling.be.global.auth.domain.CustomUserDetails;
import channeling.be.infrastructure.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

@RequiredArgsConstructor
public class MockJwtSecurityContextFactory implements WithSecurityContextFactory<WithMockJwtUser> {
    private final TestDataFactory testDataFactory;
    private final RedisUtil redisUtil;

    @Override
    public SecurityContext createSecurityContext(WithMockJwtUser annotation) {
        String googleId = annotation.googleId();

        Member member = testDataFactory.loginMockMember(googleId);
        CustomUserDetails userDetails = new CustomUserDetails(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);

        redisUtil.saveGoogleAccessToken(member.getId(), "google_access_token");
        return context;

    }
}
