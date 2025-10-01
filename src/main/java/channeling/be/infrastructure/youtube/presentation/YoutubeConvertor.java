package channeling.be.infrastructure.youtube.presentation;

import channeling.be.infrastructure.youtube.res.YoutubePlayListResDTO;

import java.util.List;

public class YoutubeConvertor {

    public static List<YoutubeDto.VideoBriefDTO> toBrief(YoutubePlayListResDTO response) {
        return response.getItems().stream()
                .filter(item -> getThumbnailUrl(item.getSnippet().getThumbnails()) != null)
                .map(item -> YoutubeDto.VideoBriefDTO.builder()
                        .videoId(item.getSnippet().getResourceId().getVideoId())
                        .thumbnailUrl(getThumbnailUrl(item.getSnippet().getThumbnails()))
                        .title(item.getSnippet().getTitle())
                        .publishedAt(item.getSnippet().getPublishedAt())
                        .build())
                .toList();
    }

    private static String getThumbnailUrl(YoutubePlayListResDTO.Thumbnails thumbnails) {
        if (thumbnails == null) return null;
        if (thumbnails.getHigh() != null) return thumbnails.getHigh().getUrl();
        if (thumbnails.getMedium() != null) return thumbnails.getMedium().getUrl();
        if (thumbnails.getDefaultThumbnail() != null) return thumbnails.getDefaultThumbnail().getUrl();
        return null;
    }
}
