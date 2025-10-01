package channeling.be.domain.channel.application;

import channeling.be.domain.channel.application.model.Stats;
import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.channel.presentation.converter.ChannelConverter;
import channeling.be.domain.channel.presentation.dto.request.ChannelRequestDto;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.application.VideoService;
import channeling.be.domain.video.domain.Video;
import channeling.be.global.infrastructure.redis.RedisUtil;
import channeling.be.global.infrastructure.youtube.dto.res.YoutubeChannelResDTO;
import channeling.be.global.infrastructure.youtube.YoutubeUtil;
import channeling.be.global.infrastructure.youtube.dto.model.YoutubeVideoBriefDTO;
import channeling.be.global.infrastructure.youtube.dto.model.YoutubeVideoDetailDTO;
import channeling.be.response.exception.handler.ChannelHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static channeling.be.response.code.status.ErrorStatus._CHANNEL_NOT_FOUND;
import static channeling.be.response.code.status.ErrorStatus._CHANNEL_NOT_MEMBER;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

//@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class ChannelServiceImpl implements ChannelService {
	private final ChannelRepository channelRepository;
	private final VideoService videoService;
	private final RedisUtil redisUtil;
	private final RestTemplate restTemplate;

	public ChannelServiceImpl(
			ChannelRepository channelRepository,
			VideoService videoService,
			RedisUtil redisUtil,
			@Qualifier("restTemplate") RestTemplate restTemplate
	) {
		this.channelRepository = channelRepository;
		this.videoService = videoService;
		this.redisUtil = redisUtil;
		this.restTemplate = restTemplate;
	}

	@AllArgsConstructor
	@Getter
	private static class YoutubeChannelVideoData {
		YoutubeChannelResDTO.Item item;
		List<YoutubeVideoBriefDTO> briefs;
		List<YoutubeVideoDetailDTO> details;
	}

    @Override
    @Transactional
    public Channel editChannelConcept(Long channelId, ChannelRequestDto.EditChannelConceptReqDto request,Member loginMember) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelHandler(_CHANNEL_NOT_FOUND));

        if (!channel.getMember().getId().equals(loginMember.getId())) {
            throw new ChannelHandler(_CHANNEL_NOT_MEMBER);
        }
        channel.editConcept(request.getConcept()); // 더티체킹
        return channel;
    }

	@Override
	public void validateChannelByIdAndMember(Long channelId,Member member) {
		Channel channel = channelRepository.findById(channelId)
			.orElseThrow(() -> new ChannelHandler(_CHANNEL_NOT_FOUND));

		if (!channel.getMember().getId().equals(member.getId())) {
			throw new ChannelHandler(_CHANNEL_NOT_MEMBER);
		}
	}

    @Override
    @Transactional
    public Channel editChannelTarget(Long channelId, ChannelRequestDto.EditChannelTargetReqDto request, Member loginMember) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelHandler(_CHANNEL_NOT_FOUND));

        if (!channel.getMember().getId().equals(loginMember.getId())) {
            throw new ChannelHandler(_CHANNEL_NOT_MEMBER);
        }
        channel.editTarget(request.getTarget()); // 더티체킹
        return channel;
    }

	@Override
	@Transactional
	public void updateChannelVideos(Channel channel, String youtubeAccessToken) {
		// List<YoutubeVideoBriefDTO> stats = YoutubeUtil.getVideoStatistics(youtubeAccessToken, channel.getJoinDate(),
		// 	LocalDateTime.now());
		// if (stats.isEmpty()) {
		// 	log.warn("채널 {}의 비디오 통계가 없습니다.", channel.getName());
		// 	return;
		// }
	}

	@Override
	@Transactional
	public Channel updateOrCreateChannelByMember(Member member) {
		Optional<Channel> channel = channelRepository.findByMember(member);
		String googleAccessToken = redisUtil.getGoogleAccessToken(member.getId());


		// 유튜브 채널 정보 가져오기
		YoutubeChannelResDTO.Item item = YoutubeUtil.getChannelDetails(
			redisUtil.getGoogleAccessToken(member.getId()));
        long shares=YoutubeUtil.getAllVideoShares(googleAccessToken, item.getSnippet().getPublishedAt(),LocalDateTime.now());
		String playlistId = item.getContentDetails().getRelatedPlaylists().getUploads();

		//유튜브 비디오 데이터 가져오기
		YoutubeChannelVideoData data = fetchYoutubeVideoData(item, googleAccessToken, playlistId);

		//유튜브 비디오 데이터(data.details)의 각 요소의 category id 의 count를 세서 가장 높은 카테고리 추출
		String topCategoryId = getTopCategoryId(data);

		//채널이 없으면 기본 1차 저장
		Channel channelEntity = channel.orElseGet(() ->
			channelRepository.save(ChannelConverter.toNewChannel(data.item, member,shares,topCategoryId))
		);

		Stats stats=updateVideosAndAccumulateStats(data.briefs, data.details, channelEntity);
		ChannelConverter.updateChannel(channelEntity, data.item, topCategoryId,stats,shares);
		return channelRepository.save(channelEntity);

	}

	private String getTopCategoryId(YoutubeChannelVideoData data) {
		return data.details.stream()
			.collect(Collectors.groupingBy(
				YoutubeVideoDetailDTO::getCategoryId,
				Collectors.counting()
			))
			.entrySet()
			.stream()
			.max(Map.Entry.comparingByValue())
			.map(Map.Entry::getKey)
			.orElse("0");
	}

	@Override
	public Channel getChannel(Long channelId, Member loggedInMember) {
		Channel channel = channelRepository.findById(channelId)
			.orElseThrow(() -> new ChannelHandler(_CHANNEL_NOT_FOUND));

		if (!channel.getMember().getId().equals(loggedInMember.getId())) {
			throw new ChannelHandler(_CHANNEL_NOT_MEMBER);
		}

		return channel;
	}

	private Stats updateVideosAndAccumulateStats(List<YoutubeVideoBriefDTO> briefs, List<YoutubeVideoDetailDTO> details, Channel channel) {
		long likeCount = 0, commentCount = 0;

		for (int i = 0; i < briefs.size(); i++) {
			YoutubeVideoBriefDTO brief = briefs.get(i);
			YoutubeVideoDetailDTO detail = details.get(i);
			likeCount += detail.getLikeCount();
			commentCount += detail.getCommentCount();
			videoService.updateVideo(brief, detail, channel);
		}
		return new Stats(likeCount, commentCount);

//		List<Video> dbVideos = videoService.findVideosByChannel(channel);
//		Set<String> briefsVideoIds = briefs.stream()
//			.map(YoutubeVideoBriefDTO::getVideoId)
//			.collect(Collectors.toSet());
//
//		for (Video dbVideo : dbVideos) {
//			if (briefsVideoIds.contains(dbVideo.getYoutubeVideoId())) {
//				// briefs에서 해당 videoId의 brief와 detail을 찾아 update
//				int idx = IntStream.range(0, briefs.size())
//					.filter(i -> briefs.get(i).getVideoId().equals(dbVideo.getYoutubeVideoId()))
//					.findFirst().orElse(-1);
//				if (idx != -1) {
//					videoService.updateVideo(briefs.get(idx), details.get(idx), channel);
//					likeCount += details.get(idx).getLikeCount();
//					commentCount += details.get(idx).getCommentCount();
//				}
//			} else {
//				videoService.deleteVideo(dbVideo);
//			}
//		}

//		return new Stats(likeCount, commentCount);
	}


	private YoutubeChannelVideoData fetchYoutubeVideoData(
		YoutubeChannelResDTO.Item item,
		String accessToken,
		String uploadPlaylistId
	) {
		List<YoutubeVideoBriefDTO> videoBriefs = YoutubeUtil.getVideosBriefsByPlayListId(accessToken, uploadPlaylistId);
		List<YoutubeVideoDetailDTO> videoDetails = YoutubeUtil.getVideoDetailsByIds(
			accessToken, videoBriefs.stream().map(YoutubeVideoBriefDTO::getVideoId).toList());

		// 비동기 작업을 담을 List 생성
		List<CompletableFuture<Void>> futures = new ArrayList<>();

		// Shorts 판별 후 categoryId 수정
		for (int i = 0; i < videoDetails.size(); i++) {
			final int index = i;
			String videoId = videoBriefs.get(i).getVideoId();

			// 각 비디오 확인 작업을 CompletableFuture로 감싸 비동기 실행
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				if (isYoutubeShorts(videoId)) {
					// TODO: 스레드 안정성 확보 필요
					videoDetails.get(index).updateCategoryId("42");
				}
			});
			futures.add(future);
		}
		// 모든 비동기 작업이 완료될 때까지 대기
		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

		return new YoutubeChannelVideoData(item, videoBriefs, videoDetails);
	}

	public boolean isYoutubeShorts(String videoId) {
		String shortsUrl = "https://www.youtube.com/shorts/" + videoId;

		try {
			ResponseEntity<String> response = restTemplate.exchange(
				shortsUrl,
				HttpMethod.HEAD,
				null,
				String.class
			);

			// 2xx 응답이고 리다이렉트가 없으면 Shorts
			if (response.getStatusCode().is2xxSuccessful()) {
				return true;
			}

			// 3xx 리다이렉트면 Location 확인
			if (response.getStatusCode().is3xxRedirection()) {
				String location = response.getHeaders().getFirst("Location");
				return location == null || !location.contains("/watch?v=");
			}

			return false; // 4xx, 5xx 에러

		} catch (HttpClientErrorException e) {
			//만약 404 에러일 경우 shorts 가 아니라고 판단
			return false;
		}
	}
}

