package channeling.be.infrastructure.youtube.dto.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class YoutubeVideoListResDTO {

	@JsonProperty("nextPageToken")
	private String nextPageToken;

	private List<Item> items;

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Item {
		private Snippet snippet;
		private Statistics statistics;
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Snippet {
		private String description;
		private String categoryId;
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Statistics{
		@JsonProperty("viewCount")
		private Long viewCount;

		@JsonProperty("likeCount")
		private Long likeCount;

		@JsonProperty("commentCount")
		private Long commentCount;
	}
}
