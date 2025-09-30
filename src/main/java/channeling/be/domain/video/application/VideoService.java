package channeling.be.domain.video.application;


import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.domain.Video;
import channeling.be.domain.video.domain.VideoCategory;
import channeling.be.domain.video.domain.VideoType;
import channeling.be.domain.video.presentaion.VideoResDTO;
import channeling.be.infrastructure.youtube.dto.YoutubeDto;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoService {
	/**
	 * 채널의 비디오 목록을 페이지 기반으로 조회합니다.
	 *
	 * @param channelId 채널 ID
	 * @param type 비디오 카테고리
	 * @param page 페이지 번호
	 * @param size 페이지 크기
	 * @return 비디오 목록의 슬라이스
	 */
	Page<VideoResDTO.VideoBrief> getChannelVideoListByType(
		Long channelId,
		VideoType type,
		int page,
		int size
	);

	/**
	 * 채널의 비디오 목록을 커서기반으로 조회합니다.
	 *
	 * @param channelId 채널 ID
	 * @param type 비디오 카테고리
	 * @param cursorUploadDate 페이지 번호
	 * @param size 페이지 크기
	 * @return 비디오 목록의 슬라이스
	 */
	Slice<VideoResDTO.VideoBrief> getChannelVideoListByTypeAfterCursor(
		Long channelId,
		VideoCategory type,
		LocalDateTime cursorUploadDate, // 커서
		int size
	);

	Video updateVideo(YoutubeDto.VideoBriefDTO briefDTO, YoutubeDto.VideoDetailDTO detailDTO, Channel channel);

	/**
	 * (홈화면) 내 채널의 추천 비디오 목록을 조회합니다.
	 *
	 * @param channelId 채널 ID
	 * @param page 페이지 번호
	 * @param size 페이지 크기
	 * @return 추천 비디오 목록의 페이지
	 */
	Page<Video> getRecommendedVideos(Long channelId, Integer page, Integer size, Member loginMember);

    Video checkVideoUrlWithMember(Member member, @NotNull(message = "null이서는 안됩니다.") String url);

	Video checkVideoWithMember(@NotNull(message = "null이서는 안됩니다.") Long videoId, Member member);

	List<Video> findVideosByChannel(Channel channel);

	void deleteVideo(Video dbVideo);
}
