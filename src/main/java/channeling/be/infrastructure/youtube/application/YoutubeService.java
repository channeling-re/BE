package channeling.be.infrastructure.youtube.application;

import channeling.be.domain.channel.application.ChannelServiceImpl;
import channeling.be.domain.channel.domain.Channel;
import channeling.be.infrastructure.youtube.presentation.YoutubeDto;
import channeling.be.infrastructure.youtube.res.YoutubeChannelResDTO;

import java.util.List;

/*
 * HTTP 요청을 유튜브 API에 요청하기 위한 유틸리티 클래스
 */
public interface YoutubeService {
    /*
     * 유튜브 API를 통해 동영상 정보를 동기화합니다.
     */
    void syncVideos(YoutubeChannelResDTO.Item item, String accessToken, Channel channel);

    /*
     * 유튜브 API를 통해 채널 정보를 동기화합니다.
     */
    YoutubeChannelResDTO.Item syncChannel(String accessToken);

    /*
     * 유튜브 API를 통해 채널의 동영상 정보를 가져옵니다.
     * 1. getYoutubePlayLists() : 재생목록에서 동영상 목록을 가져옵니다.
     * 2. getYoutubeVideoDetail() : 동영상 ID 목록으로 동영상의 상세 정보를 가져옵니다.
     */
    ChannelServiceImpl.YoutubeChannelVideoData getVideos(YoutubeChannelResDTO.Item item, String accessToken, String uploadPlaylistId);
    List<YoutubeDto.VideoBriefDTO> getYoutubePlayLists(String accessToken, String playlistId);
    List<YoutubeDto.VideoDetailDTO> getYoutubeVideoDetail(String accessToken, List<String> videoIds);

    /*
     * 유튜브 쇼츠 여부를 판단합니다.
     */
    boolean isYoutubeShorts(String videoId);

}
