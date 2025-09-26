package channeling.be.infrastructure.youtube.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class YoutubeVideoBriefDTO {
	private final String videoId;
	private final String thumbnailUrl;
	private final String title;
	private final String publishedAt;
}
