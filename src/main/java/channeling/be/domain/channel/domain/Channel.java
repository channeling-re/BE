package channeling.be.domain.channel.domain;

import channeling.be.domain.common.BaseEntity;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.domain.VideoCategory;
import channeling.be.infrastructure.youtube.res.YoutubeChannelResDTO;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;

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
    @Builder.Default
    private Long view = 1L; // 조회수

    @Column
    private Long likeCount; // 좋아요 수

    @Column(nullable = false)
    @Builder.Default
    private Long subscribe = 0L; // 구독자 수

    @Column
    private Long share; // 공유 수

    @Column(nullable = false)
    @Builder.Default
    private Long videoCount = 0L; // 영상 수

    @Column
    private Long comment; // 체널 총 댓글 수

    @Column(nullable = false, length = 150)
    private String link; // 채널 링크

    @Column(nullable = false)
    private LocalDateTime joinDate; // 채널 가입일

    @Column(length = 100)
    private String target; // 시청자 타겟

    @Column(length = 500)
    private String concept; // 채널 컨셉

    @Column(nullable = false)
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

    // 비디오, 통계관련 컬럼 업데이트
    public void updateChannelStats(Long totalLikeCount, Long totalCommentCount, String topCategory) {
        // this.share=shares; // TODO : 추후 공유수 반영
        this.likeCount = totalLikeCount;
        this.comment = totalCommentCount;
        this.channelHashTag = VideoCategory.ofId(topCategory);
        this.channelUpdateAt = LocalDateTime.now();
    }

    // 유튜브 채널 정보 업데이트
    public void updateByYoutube(YoutubeChannelResDTO.Item item) {
        this.name = item.getSnippet().getTitle();
        this.youtubeChannelId = item.getId();
        this.youtubePlaylistId = item.getContentDetails().getRelatedPlaylists().getUploads();
        this.image = item.getSnippet().getThumbnails().getDefaultThumbnail().getUrl();
        this.link = "https://www.youtube.com/channel/" + item.getId();
        this.joinDate = item.getSnippet().getPublishedAt();
        this.view = item.getStatistics().getViewCount();
        this.subscribe = item.getStatistics().getSubscriberCount();
        this.videoCount = item.getStatistics().getVideoCount();
    }

}
