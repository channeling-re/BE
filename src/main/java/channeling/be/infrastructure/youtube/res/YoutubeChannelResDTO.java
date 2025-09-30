package channeling.be.infrastructure.youtube.res;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class YoutubeChannelResDTO {
	private List<Item> items;

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	public static class Item {
		private String id;
		private Snippet snippet;
		private Statistics statistics;
		private ContentDetails contentDetails;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	public static class Snippet {
		private String title;

		@JsonProperty("customUrl")
		private String customUrl;

		private LocalDateTime publishedAt;
		private Thumbnails thumbnails;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	public static class Thumbnails {
		@JsonProperty("default")
		private Thumbnail defaultThumbnail;
		// private Thumbnail medium;
		// private Thumbnail high;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	public static class Thumbnail {
		private String url;
		private int width;
		private int height;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	public static class Statistics {
		private Long viewCount;
		private Long subscriberCount;
		private Long videoCount;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	public static class ContentDetails {
		private RelatedPlaylists relatedPlaylists;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	public static class RelatedPlaylists {
		private String uploads;
		// private String likes;
	}
}
