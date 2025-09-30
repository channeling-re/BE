package channeling.be.infrastructure.youtube.application;

import channeling.be.domain.channel.application.ChannelServiceImpl;
import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.video.application.VideoService;
import channeling.be.infrastructure.youtube.YoutubeConvertor;
import channeling.be.infrastructure.youtube.dto.model.YoutubeVideoBriefDTO;
import channeling.be.infrastructure.youtube.dto.model.YoutubeVideoDetailDTO;
import channeling.be.infrastructure.youtube.dto.model.YoutubeVideoListResDTO;
import channeling.be.infrastructure.youtube.dto.res.YoutubeChannelResDTO;
import channeling.be.infrastructure.youtube.dto.res.YoutubePlayListResDTO;
import channeling.be.response.code.status.ErrorStatus;
import channeling.be.response.exception.handler.YoutubeHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class YoutubeUtil implements YoutubeService {

    private final RestTemplate restTemplate;
    private final VideoService videoService;

    private static final String YOUTUBE_API_BASE_URL = "https://www.googleapis.com/youtube/v3";

    // 채널과 연관된 비디오들을 동기화하는 메서드
    public void syncVideos(YoutubeChannelResDTO.Item item, String accessToken, Channel channel) {
        // 비디오 목록 불러오기
        String playlistId = item.getContentDetails().getRelatedPlaylists().getUploads();
        ChannelServiceImpl.YoutubeChannelVideoData data = getVideos(item, accessToken, playlistId);

        // 채널 통계 업데이트
        String topCategoryId = getTopCategoryId(data); // 유튜브 비디오 중 가장 많은 category
        Long totalLike = data.getDetails().stream().mapToLong(YoutubeVideoDetailDTO::getLikeCount).sum();
        Long totalComment = data.getDetails().stream().mapToLong(YoutubeVideoDetailDTO::getCommentCount).sum();

        channel.updateChannelStats(totalLike, totalComment, topCategoryId);

        // 비디오 정보 업데이트
        for (int i = 0; i < data.getDetails().size(); i++) {
            YoutubeVideoBriefDTO brief = data.getBriefs().get(i);
            YoutubeVideoDetailDTO detail = data.getDetails().get(i);
            videoService.updateVideo(brief, detail, channel);
        }
    }


    // 유튜브 API를 호출하여 채널의 정보를 가져오는 메서드
    public YoutubeChannelResDTO.Item syncChannel(String accessToken) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(YOUTUBE_API_BASE_URL + "/channels")
                    .queryParam("part", "snippet,contentDetails,statistics")
                    .queryParam("mine", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            ResponseEntity<YoutubeChannelResDTO> response = restTemplate.exchange(
                    builder.build().toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    YoutubeChannelResDTO.class
            );

            log.info("googleAccessToken: {}", accessToken);
            log.info("Channel Response: {}", response.getBody());

            YoutubeChannelResDTO youtubeResponse = response.getBody();
            if (youtubeResponse.getItems() == null || youtubeResponse.getItems().isEmpty()) {
                throw new YoutubeHandler(ErrorStatus._YOUTUBE_CHANNEL_PULLING_ERROR);
            }

            return youtubeResponse.getItems().get(0);
        } catch (Exception e) {
            log.error("Failed to fetch channel details", e);
            throw new YoutubeHandler(ErrorStatus._YOUTUBE_CHANNEL_PULLING_ERROR);
        }
    }

    // 비디오 목록 불러오기
    public ChannelServiceImpl.YoutubeChannelVideoData getVideos(
            YoutubeChannelResDTO.Item item,
            String accessToken,
            String uploadPlaylistId
    ) {
        // yotube 비디오 요약 정보 및 상세 정보 불러오기
        List<YoutubeVideoBriefDTO> videoBriefs = getYoutubePlayLists(accessToken, uploadPlaylistId);
        List<String> videoIds = videoBriefs.stream()
                .map(YoutubeVideoBriefDTO::getVideoId)
                .toList();
        List<YoutubeVideoDetailDTO> videoDetails = getYoutubeVideoDetail(accessToken, videoIds);

        // Shorts 여부 확인 및 카테고리 업데이트
        for (int i = 0; i < videoDetails.size(); i++) {
            if (isYoutubeShorts(videoBriefs.get(i).getVideoId())) {
                videoDetails.get(i).updateCategoryId("42");
            }
        }
        return new ChannelServiceImpl.YoutubeChannelVideoData(item, videoBriefs, videoDetails);
    }


    // 유튜브 API를 호출하여 플레이리스트의 비디오 정보를 가져오는 메서드
    public List<YoutubeVideoBriefDTO> getYoutubePlayLists(String accessToken, String playlistId) {
        List<YoutubeVideoBriefDTO> videoList = new ArrayList<>();
        String pageToken = null;

        do {
            YoutubePlayListResDTO briefs = getYoutubePlayList(accessToken, playlistId);
            videoList.addAll(YoutubeConvertor.toBrief(briefs));
            pageToken = briefs.getNextPageToken();
        } while (pageToken != null);

        return videoList;
    }

    // 유튜브 플레이리스트 정보 가져오기
    private YoutubePlayListResDTO getYoutubePlayList(String accessToken, String playlistId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(YOUTUBE_API_BASE_URL + "/playlistItems")
                    .queryParam("part", "snippet,contentDetails")
                    .queryParam("playlistId", playlistId)
                    .queryParam("maxResults", 50);

            ResponseEntity<YoutubePlayListResDTO> response = restTemplate.exchange(
                    builder.build().toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    YoutubePlayListResDTO.class
            );

            YoutubePlayListResDTO youtubeResponse = response.getBody();
            log.info("response: {}", youtubeResponse);

            if (youtubeResponse.getItems() == null) {
                log.error("유튜브 플레이리스트 조회 중 에러 발생: items is null");
                throw new YoutubeHandler(ErrorStatus._YOUTUBE_PLAYLIST_PULLING_ERROR);
            }

            return youtubeResponse;
        } catch (Exception e) {
            log.error("유튜브 플레이리스트 조회 중 에러 발생", e);
            throw new YoutubeHandler(ErrorStatus._YOUTUBE_PLAYLIST_PULLING_ERROR);
        }
    }


    // 유튜브 비디오의 상세 정보를 가져오는 메서드
    public List<YoutubeVideoDetailDTO> getYoutubeVideoDetail(String accessToken, List<String> videoIds) {
        try {
            String ids = String.join(",", videoIds);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(YOUTUBE_API_BASE_URL + "/videos")
                    .queryParam("part", "snippet,statistics")
                    .queryParam("id", ids);

            ResponseEntity<YoutubeVideoListResDTO> response = restTemplate.exchange(
                    builder.build().toUriString(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    YoutubeVideoListResDTO.class
            );

            YoutubeVideoListResDTO youtubeVideoListResDTO = response.getBody();
            if (youtubeVideoListResDTO.getItems() == null) {
                log.error("유튜브 비디오 상세보기 조회 중 에러 발생: items is null");
                throw new YoutubeHandler(ErrorStatus._YOUTUBE_PLAYLIST_PULLING_ERROR);
            }

            return youtubeVideoListResDTO.getItems().stream()
                    .map(item -> new YoutubeVideoDetailDTO(
                            item.getSnippet().getDescription(),
                            item.getSnippet().getCategoryId(),
                            item.getStatistics().getViewCount(),
                            item.getStatistics().getLikeCount(),
                            item.getStatistics().getCommentCount()
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch video details: " + e.getMessage(), e);
        }
    }

    // 유튜브 숏츠 여부 확인
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
                return !location.contains("/watch?v=");
            }

            return false; // 4xx, 5xx 에러
        } catch (HttpClientErrorException e) {
            return false; //만약 404 에러일 경우 shorts 가 아니라고 판단
        }
    }

    // 채널 내 비디오의 가장 많은 카테고리 ID를 가져오는 메서드
    private String getTopCategoryId(ChannelServiceImpl.YoutubeChannelVideoData data) {
        return data.getDetails().stream()
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
}
