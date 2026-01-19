package channeling.be.global;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.domain.Video;
import channeling.be.domain.video.domain.repository.VideoRepository;
import channeling.be.global.annotation.WithMockJwtUser;
import channeling.be.global.auth.domain.CustomUserDetails;
import channeling.be.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class WithMockJwtUserIntegrationTest extends IntegrationTestSupport {

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    VideoRepository videoRepository;

    @Test
    @WithMockJwtUser(googleId = "test_google_id")
    void WithMockJwtUser가_Member와_연관데이터를_정상_주입한다(Member member) {

        // 1 Member 파라미터 주입 검증
        assertNotNull(member);
        assertNotNull(member.getId());
        assertEquals("test_google_id", member.getGoogleId());

        // 2 SecurityContext principal 검증
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.getPrincipal() instanceof CustomUserDetails);

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        assertEquals(member.getId(), userDetails.getMember().getId());

        // 3️ Channel 생성 여부 검증
        Channel channel = channelRepository.findByMember(member)
                .orElseThrow(() -> new AssertionError("Channel not created"));

        // 4 Video 생성 여부 검증
        List<Video> videos = videoRepository.findByChannel(channel);

        assertFalse(videos.isEmpty());
        assertEquals(3, videos.size());
    }
}
