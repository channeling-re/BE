package channeling.be.domain.channel.domain;

import channeling.be.domain.common.BaseEntity;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.domain.VideoCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Channel extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column(nullable = false, unique = true, length = 30)
    private String youtubeChannelId; // 채널 ID (유튜브 채널 ID)

    @Column(nullable = false, unique = true, length = 30)
    private String youtubePlaylistId; // 플레이리스트 ID (유튜브 플레이리스트 ID)

    @Column(length = 50, nullable = false)
    private String name; // 채널 이름

    @Column(nullable = false)
    private Long view; // 조회수

    @Column(nullable = false)
    private Long likeCount; // 좋아요 수

    @Column(nullable = false)
    private Long subscribe; // 구독자 수

    @Column(nullable = false)
    private Long share; // 공유 수

    @Column(nullable = false)
    private Long videoCount; // 영상 수

    @Column(nullable = false)
    private Long comment; // 체널 총 댓글 수

    @Column(nullable = false, length = 150)
    private String link; // 채널 링크

    @Column(nullable = false)
    private LocalDateTime joinDate; // 채널 가입일

    @Column(length = 100, nullable = false)
    private String target; // 시청자 타겟

    @Column(length = 500, nullable = false)
    private String concept; // 채널 컨셉

    @Column(nullable = false, length = 255)
    private String image; // 채널 프로필 이미지

    @Enumerated(EnumType.STRING)
    private VideoCategory channelHashTag; // 채널 해시태그

    @Column(nullable = false)
    private LocalDateTime channelUpdateAt; // 채널 정보 업데이트 시기


    public void editConcept(String concept) {
        this.concept = concept;
    }
    public void editTarget(String target) {
        this.target = target;
    }

    public void updateChannelStats(Long totalLikeCount, Long totalCommentCount) {
        this.likeCount = totalLikeCount;
        this.comment = totalCommentCount;
        this.channelUpdateAt = LocalDateTime.now();
    }

    public void updateChannelInfo(String title, String channelId, String uploadPlaylistId, String profileImageUrl, String channelUrl, LocalDateTime publishedAt, Long viewCount, Long subscriberCount, Long videoCount,
        long likeCount, long commentCount,String topCategoryId,long shares) {
        this.name = title;
        this.youtubeChannelId = channelId;
        this.youtubePlaylistId = uploadPlaylistId;
        this.image = profileImageUrl;
        this.link = channelUrl;
        this.joinDate = publishedAt;
        this.view = viewCount;
        this.subscribe = subscriberCount;
        this.videoCount = videoCount;
        this.likeCount = likeCount;
        this.comment = commentCount;
        this.channelUpdateAt = LocalDateTime.now();
        this.channelHashTag = VideoCategory.ofId(topCategoryId);
        this.share=shares;
    }
}
