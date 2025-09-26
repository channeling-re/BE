package channeling.be.domain.video.domain;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.infrastructure.youtube.dto.model.YoutubeVideoBriefDTO;
import channeling.be.infrastructure.youtube.dto.model.YoutubeVideoDetailDTO;

public class VideoConverter {
	public static Video toVideo(YoutubeVideoBriefDTO briefDTO, YoutubeVideoDetailDTO detailDTO, Channel channel) {
		return Video.builder()
			.channel(channel)
			.youtubeVideoId(briefDTO.getVideoId())
			.view(detailDTO.getViewCount())
			.likeCount(detailDTO.getLikeCount())
			.commentCount(detailDTO.getCommentCount())
			.thumbnail(briefDTO.getThumbnailUrl())
			.title(briefDTO.getTitle())
			.description(detailDTO.getDescription())
			.link("https://www.youtube.com/watch?v="+briefDTO.getVideoId())
			.uploadDate(OffsetDateTime.parse(briefDTO.getPublishedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME
			).toLocalDateTime())
			.videoCategory(VideoCategory.ofId(detailDTO.getCategoryId()))
			.build();
	}
	public static void toVideo(Video video,YoutubeVideoBriefDTO briefDTO, YoutubeVideoDetailDTO detailDTO) {
		video.setLink("https://www.youtube.com/watch?v=" + briefDTO.getVideoId());
		video.setThumbnail(briefDTO.getThumbnailUrl());
		video.setTitle(briefDTO.getTitle());
		video.setUploadDate(OffsetDateTime.parse(briefDTO.getPublishedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
			.toLocalDateTime());
		video.setDescription(detailDTO.getDescription());
		video.setView(detailDTO.getViewCount());
		video.setLikeCount(detailDTO.getLikeCount());
		video.setCommentCount(detailDTO.getCommentCount());
		video.setVideoCategory(
				VideoCategory.ofId(detailDTO.getCategoryId())
		);
	}
}
