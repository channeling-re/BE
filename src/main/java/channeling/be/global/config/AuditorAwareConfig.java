package channeling.be.global.config;

import channeling.be.domain.auth.domain.CustomUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Optional;

public class AuditorAwareConfig implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    if (principal instanceof CustomUserDetails customUser) {
                        return customUser.getMember().getGoogleEmail(); // JWT 유저네임
                    }
                    if (principal instanceof DefaultOAuth2User oAuth2User) {
                        return oAuth2User.getAttribute("email"); // 구글 이메일
                    }
                    return null;
                });
    }
}
