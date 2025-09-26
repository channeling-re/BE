package channeling.be.infrastructure.youtube.dto.res;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class YoutubePlayListResDTO {

	@JsonProperty("nextPageToken")
	private String nextPageToken;

	private List<Item> items;

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	public static class Item {
		private Snippet snippet;
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Snippet {
		private String title;
		private String publishedAt;
		private ResourceId resourceId;
		private Thumbnails thumbnails;
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ResourceId {
		private String videoId;
	}

	@Getter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Thumbnails {
		@JsonProperty("default")
		private Thumbnail defaultThumbnail;
		private Thumbnail medium;
		private Thumbnail high;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	public static class Thumbnail {
		private String url;
		private int width;
		private int height;
	}
}
