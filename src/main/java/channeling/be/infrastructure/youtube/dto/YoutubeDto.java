package channeling.be.infrastructure.youtube.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class YoutubeDto {
    @Getter
    @Builder
    @AllArgsConstructor
    public record VideoBriefDTO(
        String videoId,
        String thumbnailUrl,
        String title,
        String publishedAt
    ) {}
}
