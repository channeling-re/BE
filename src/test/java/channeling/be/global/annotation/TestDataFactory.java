package channeling.be.global.annotation;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.member.domain.repository.MemberRepository;
import channeling.be.domain.video.domain.Video;
import channeling.be.domain.video.domain.VideoCategory;
import channeling.be.domain.video.domain.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataFactory {
    private final MemberRepository memberRepository;
    private final ChannelRepository channelRepository;
    private final VideoRepository videoRepository;
    /* 단일 엔티티 생성   */
    private Member createMember(String googleId) {
        Member member = Member.builder()
                .nickname("test")
                .googleId(googleId)
                .googleEmail("test@google.com")
                .isDeleted(false)
                .build();
        return memberRepository.save(member);
    }

    private Channel createChannel(Member member, String name) {
        Channel channel = Channel.builder()
                .member(member)
                .name(name)
                .link("testlink")
                .image("testimage")
                .youtubeChannelId("test")
                .youtubePlaylistId("test")
                .channelUpdateAt(LocalDateTime.now())
                .joinDate(LocalDateTime.now())
                .build();
        return channelRepository.save(channel);
    }

    private List<Video> createVideos(Channel channel, int count) {
        List<Video> videos = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            videos.add(createVideo(channel, i));
        }

        return videos;

    }


    private static Video createVideo(Channel channel, int idx) {
        return Video.builder()
                .channel(channel)
                .youtubeVideoId("yt_video_" + idx)
                .videoCategory(idx % 2 == 0 ? VideoCategory.SHORTS : VideoCategory.FILM_AND_ANIMATION)
                .title("테스트 영상 " + idx)
                .view(1000L * idx)
                .likeCount(100L * idx)
                .commentCount(10L * idx)
                .link("https://youtube.com/watch?v=yt_video_" + idx)
                .uploadDate(LocalDateTime.now().minusDays(idx))
                .thumbnail("thumb" + idx + ".jpg")
                .description("테스트 영상 설명 " + idx)
                .build();
    }


    @Transactional
    public Member loginMockMember(String googleId) {

        // 1️⃣ Member 생성
        Member member = createMember(googleId);

        // 2️⃣ Channel 생성
        Channel channel = createChannel(member, "test-channel");

        // 3️⃣ Video 생성 (연관관계만 설정)
        List<Video> videos = createVideos(channel, 3);
        videoRepository.saveAll(videos);

        return member;
    }

}
