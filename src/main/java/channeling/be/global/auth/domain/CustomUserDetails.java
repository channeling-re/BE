package channeling.be.global.auth.domain;

import channeling.be.domain.member.domain.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Member member;

    public CustomUserDetails(Member member) {
        this.member = member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 역할(권한)을 사용하지 않는다면 빈 리스트 반환
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return member.getGoogleId();  // 또는 memberId 등
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;  // 필요 시 구현
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;  // 필요 시 구현
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;  // 필요 시 구현
    }

    @Override
    public boolean isEnabled() {
        return true;  // 필요 시 구현
    }

    public Member getMember() {
        return member;
    }
}

