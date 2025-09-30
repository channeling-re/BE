package channeling.be.domain.video.domain;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.infrastructure.youtube.dto.YoutubeDto;

public class VideoConverter {
	public static Video toVideo(YoutubeDto.VideoBriefDTO briefDTO, YoutubeDto.VideoDetailDTO detailDTO, Channel channel) {
		return Video.builder()
			.channel(channel)
			.youtubeVideoId(briefDTO.videoId())
			.view(detailDTO.viewCount())
			.likeCount(detailDTO.likeCount())
			.commentCount(detailDTO.commentCount())
			.thumbnail(briefDTO.thumbnailUrl())
			.title(briefDTO.title())
			.description(detailDTO.description())
			.link("https://www.youtube.com/watch?v="+briefDTO.videoId())
			.uploadDate(OffsetDateTime.parse(briefDTO.publishedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME
			).toLocalDateTime())
			.videoCategory(VideoCategory.ofId(detailDTO.categoryId()))
			.build();
	}
	public static void toVideo(Video video, YoutubeDto.VideoBriefDTO briefDTO, YoutubeDto.VideoDetailDTO detailDTO) {
		video.setLink("https://www.youtube.com/watch?v=" + briefDTO.videoId());
		video.setThumbnail(briefDTO.thumbnailUrl());
		video.setTitle(briefDTO.title());
		video.setUploadDate(OffsetDateTime.parse(briefDTO.publishedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
			.toLocalDateTime());
		video.setDescription(detailDTO.description());
		video.setView(detailDTO.viewCount());
		video.setLikeCount(detailDTO.likeCount());
		video.setCommentCount(detailDTO.commentCount());
		video.setVideoCategory(
				VideoCategory.ofId(detailDTO.categoryId())
		);
	}
}
