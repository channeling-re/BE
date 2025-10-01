package channeling.be.domain.auth.application;

import channeling.be.domain.auth.domain.CustomUserDetails;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String googleId) throws UsernameNotFoundException {
        Member member = memberRepository.findByGoogleId(googleId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with googleId: " + googleId ));

        if (member.getIsDeleted()) {
            throw new UsernameNotFoundException("Deleted user found with googleId: " + googleId);
        }
        return new CustomUserDetails(member);
    }
}

