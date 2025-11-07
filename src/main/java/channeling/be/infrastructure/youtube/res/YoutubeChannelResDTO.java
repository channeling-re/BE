package channeling.be.infrastructure.youtube.res;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;


@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@ToString
public class YoutubeChannelResDTO {
	private List<Item> items;

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	@ToString
	public static class Item {
		private String id;
		private Snippet snippet;
		private Statistics statistics;
		private ContentDetails contentDetails;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	@ToString
	public static class Snippet {
		private String title;

		@JsonProperty("customUrl")
		private String customUrl;

		private LocalDateTime publishedAt;
		private Thumbnails thumbnails;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	@ToString
	public static class Thumbnails {
		private Thumbnail defaultThumbnail;
		// private Thumbnail medium;
		// private Thumbnail high;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	@ToString
	public static class Thumbnail {
		private String url;
		private int width;
		private int height;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	@ToString
	public static class Statistics {
		private Long viewCount;
		private Long subscriberCount;
		private Long videoCount;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	@ToString
	public static class ContentDetails {
		private RelatedPlaylists relatedPlaylists;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	@Getter
	@ToString
	public static class RelatedPlaylists {
		private String uploads;
		// private String likes;
	}
}
