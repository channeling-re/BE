package channeling.be.infrastructure.youtube.dto;

import lombok.Builder;

public class YoutubeDto {
    @Builder
    public record VideoBriefDTO(
        String videoId,
        String thumbnailUrl,
        String title,
        String publishedAt
    ) {}

    @Builder
    public record VideoDetailDTO(
        String description,
        String categoryId,
        Long viewCount,
        Long likeCount,
        Long commentCount
    ) {
        public VideoDetailDTO withCategoryId(String newCategoryId) {
            return VideoDetailDTO.builder()
                    .description(description)
                    .categoryId(newCategoryId)
                    .viewCount(viewCount)
                    .likeCount(likeCount)
                    .commentCount(commentCount)
                    .build();
        }
    }
}
