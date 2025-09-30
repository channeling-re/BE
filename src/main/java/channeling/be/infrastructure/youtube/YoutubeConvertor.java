package channeling.be.infrastructure.youtube;

import channeling.be.infrastructure.youtube.dto.model.YoutubeVideoBriefDTO;
import channeling.be.infrastructure.youtube.dto.res.YoutubePlayListResDTO;

import java.util.List;

public class YoutubeConvertor {

    public static List<YoutubeVideoBriefDTO> toBrief(YoutubePlayListResDTO response) {
        return response.getItems().stream()
                .filter(item -> getThumbnailUrl(item.getSnippet().getThumbnails()) != null)
                .map(item -> new YoutubeVideoBriefDTO(
                        item.getSnippet().getResourceId().getVideoId(),
                        getThumbnailUrl(item.getSnippet().getThumbnails()),
                        item.getSnippet().getTitle(),
                        item.getSnippet().getPublishedAt()
                ))
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
