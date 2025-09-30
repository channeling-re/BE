package channeling.be.domain.video.domain;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.infrastructure.youtube.dto.YoutubeDto;
import channeling.be.infrastructure.youtube.dto.model.YoutubeVideoDetailDTO;

public class VideoConverter {
	public static Video toVideo(YoutubeDto.VideoBriefDTO briefDTO, YoutubeVideoDetailDTO detailDTO, Channel channel) {
		return Video.builder()
			.channel(channel)
			.youtubeVideoId(briefDTO.videoId())
			.view(detailDTO.getViewCount())
			.likeCount(detailDTO.getLikeCount())
			.commentCount(detailDTO.getCommentCount())
			.thumbnail(briefDTO.thumbnailUrl())
			.title(briefDTO.title())
			.description(detailDTO.getDescription())
			.link("https://www.youtube.com/watch?v="+briefDTO.videoId())
			.uploadDate(OffsetDateTime.parse(briefDTO.publishedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME
			).toLocalDateTime())
			.videoCategory(VideoCategory.ofId(detailDTO.getCategoryId()))
			.build();
	}
	public static void toVideo(Video video, YoutubeDto.VideoBriefDTO briefDTO, YoutubeVideoDetailDTO detailDTO) {
		video.setLink("https://www.youtube.com/watch?v=" + briefDTO.videoId());
		video.setThumbnail(briefDTO.thumbnailUrl());
		video.setTitle(briefDTO.title());
		video.setUploadDate(OffsetDateTime.parse(briefDTO.publishedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
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
