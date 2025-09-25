package channeling.be.global.config;

import channeling.be.domain.auth.domain.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Optional;

@Slf4j
public class AuditorAwareConfig implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() { // 2. 반환 타입도 Optional<String>으로 변경
        return Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    if (principal instanceof CustomUserDetails c) {
                        return c.getMember().getId().toString();
                    }
                    if (principal instanceof DefaultOAuth2User) {
                        return ((DefaultOAuth2User) principal).getAttribute("email");
                    }
                    return null;
                });
    }
}
