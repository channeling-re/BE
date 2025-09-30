package channeling.be.domain.video.application;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.domain.Video;
import channeling.be.domain.video.domain.VideoCategory;
import channeling.be.domain.video.domain.VideoConverter;
import channeling.be.domain.video.domain.VideoType;
import channeling.be.domain.video.domain.repository.VideoRepository;
import channeling.be.domain.video.presentaion.VideoResDTO;
import channeling.be.infrastructure.youtube.dto.YoutubeDto;
import channeling.be.infrastructure.youtube.dto.model.YoutubeVideoDetailDTO;
import channeling.be.response.code.status.ErrorStatus;
import channeling.be.response.exception.handler.ChannelHandler;
import channeling.be.response.exception.handler.VideoHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static channeling.be.response.code.status.ErrorStatus._CHANNEL_NOT_FOUND;
import static channeling.be.response.code.status.ErrorStatus._CHANNEL_NOT_MEMBER;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class VideoServiceImpl implements VideoService {

	private final VideoRepository videoRepository;
	private final ChannelRepository channelRepository;

	@Override
	public Page<VideoResDTO.VideoBrief> getChannelVideoListByType(Long channelId, VideoType type ,int page, int size) {
		Pageable pageable = PageRequest.of(page-1, size);
		Page<Video> videos;
		if(VideoType.SHORTS.equals(type)) {
			videos=videoRepository.findByChannelIdAndVideoCategoryOrderByUploadDateDesc(channelId, VideoCategory.SHORTS, pageable);
		} else{
			videos=videoRepository.findByChannelIdAndVideoCategoryNotOrderByUploadDateDesc(channelId,VideoCategory.SHORTS,pageable);
		}

		return videos.map(VideoResDTO.VideoBrief::from);
	}

	@Deprecated
	@Override
	public Slice<VideoResDTO.VideoBrief> getChannelVideoListByTypeAfterCursor(Long channelId, VideoCategory type,
		LocalDateTime cursor, int size) {
		Pageable pageable = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "uploadDate"));

		Slice<Video> videos= (cursor==null)
			? videoRepository.findByChannelIdAndVideoCategoryOrderByUploadDateDesc(channelId,type,pageable)
			: videoRepository.findByChannelIdAndVideoCategoryAndUploadDateLessThanOrderByUploadDateDesc(channelId, type, cursor, pageable);

		return videos.map(VideoResDTO.VideoBrief::from);
	}

	@Transactional
	@Override
	public Video updateVideo(YoutubeDto.VideoBriefDTO briefDTO, YoutubeVideoDetailDTO detailDTO, Channel channel) {
		Optional<Video> existing = videoRepository.findByYoutubeVideoId(briefDTO.videoId());

		if (existing.isPresent()) {
			Video original = existing.get();
			VideoConverter.toVideo(original, briefDTO, detailDTO);
			return videoRepository.save(original);
		} else {
			log.info("briefDTO = {}", briefDTO);
			return videoRepository.save(VideoConverter.toVideo(briefDTO,detailDTO,channel));
		}
	}

	@Override
	public Page<Video> getRecommendedVideos(Long channelId, Integer page, Integer size, Member loginMember) {
		Channel channel = channelRepository.findById(channelId)
				.orElseThrow(() -> new ChannelHandler(_CHANNEL_NOT_FOUND));

		if (!channel.getMember().getId().equals(loginMember.getId())) {
			throw new ChannelHandler(_CHANNEL_NOT_MEMBER);
		}

		return videoRepository.findAllRecommendationByChannel(channel.getId(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "view")));
	}


	// http:// 또는 https://
	// www 있을 수도, 없을 수도.
	// youtube.com/watch?v=” 이거나 “youtu.be/” 라는 문자열
	// 유튜브 아이디는 [...] 문자 집합 {11} 11글자
	private static final Pattern YOUTUBE_URL_PATTERN = Pattern.compile(
			"^(?:https?://)?(?:www\\.)?(?:youtube\\.com|youtu\\.be)/.*?([\\w-]{11})(?:[^\\w-]|$)",
			Pattern.CASE_INSENSITIVE
	);

	// 유튜브 URL에서 videoId 추출
	public static String extractYoutubeVideoId(String url) {
		if (url == null || url.isBlank()) return null;
		Matcher matcher = YOUTUBE_URL_PATTERN.matcher(url);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	// 유효한 유튜브 URL인지 확인 (videoId 추출 성공 여부로 판단)
	public static boolean isValidYoutubeUrl(String url) {
		return extractYoutubeVideoId(url) != null;
	}


	// 멤버와 URL이 일치하는 영상인지 체크하는 메서드
	public Video checkVideoUrlWithMember(Member member, String url) {
		// 1. URL 유효성 검사
		if (!isValidYoutubeUrl(url)) {
			throw new VideoHandler(ErrorStatus._LINK_NOT_VALID);
		}

		// 2. youtubeVideoId 추출 (정규화)
		String youtubeVideoId = extractYoutubeVideoId(url);

		// 3. DB에서 멤버 소유 영상 존재 여부 확인

        return videoRepository.findByMemberAndYoutubeVideoId(member.getId(), youtubeVideoId)
                .orElseThrow(() -> new VideoHandler(ErrorStatus._VIDEO_NOT_MEMBER));

		}

	@Override
	public Video checkVideoWithMember(Long videoId, Member member) {
		Video video = videoRepository.findById(videoId)
				.orElseThrow(() -> new VideoHandler(ErrorStatus._VIDEO_NOT_FOUND));

		if (!video.getChannel().getMember().getId().equals(member.getId())) {
			throw new VideoHandler(ErrorStatus._VIDEO_NOT_MEMBER);
		}

		return video;
	}

	@Override
	public List<Video> findVideosByChannel(Channel channel) {
		return videoRepository.findByChannelId((channel.getId()));
	}

	@Override
	@Transactional
	public void deleteVideo(Video dbVideo) {
		videoRepository.delete(dbVideo);
	}
}






