package channeling.be.domain.video.domain.repository;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.video.domain.Video;
import channeling.be.domain.video.domain.VideoCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface VideoRepository extends JpaRepository<Video, Long> {

	Slice<Video> findByChannelIdAndVideoCategory(Long channelId, VideoCategory videoCategory, Pageable pageable);

	Page<Video> findByChannelIdAndVideoCategoryOrderByUploadDateDesc(Long channelId, VideoCategory type, Pageable pageable);

	Page<Video> findByChannelIdAndVideoCategoryNotOrderByUploadDateDesc(Long channelId, VideoCategory type, Pageable pageable);

	Slice<Video> findByChannelIdAndVideoCategoryAndUploadDateLessThanOrderByUploadDateDesc(Long channelId, VideoCategory type, LocalDateTime cursor, Pageable pageable);

	Optional<Video> findByYoutubeVideoId(String youtubeVideoId);

	@Query("""
    SELECT v
    FROM Video v
    JOIN v.channel c
    JOIN c.member m
    WHERE v.id = :videoId AND m.id = :memberId
""")
	Optional<Video> findByIdWithMemberId(@Param("videoId")Long videoId, @Param("memberId")Long memberId);

	// 내 채널 추천 영상 : 조회 수 높은 영상, 리포트 안 받아본 영상
	@Query("SELECT v FROM Video v " +
			"LEFT JOIN FETCH Report r ON v.id = r.video.id " +
			"JOIN FETCH v.channel " +
			"WHERE v.channel.id = :channelId " +
			"AND r.id is null " +
			"ORDER BY v.view DESC ")
	Page<Video> findAllRecommendationByChannel(@Param("channelId") Long channelId, Pageable pageable);


	@Query("""
        SELECT v
        FROM Video v
        JOIN v.channel c
        JOIN c.member m
        WHERE m.id = :memberId
	    AND v.youtubeVideoId = :youtubeVideoId
""")
	Optional<Video> findByMemberAndYoutubeVideoId(@Param("memberId") Long memberId, @Param("youtubeVideoId") String youtubeVideoId);

	List<Video> findByChannelId(Long channelId);

    List<Video> findByChannel(Channel channel);
}
