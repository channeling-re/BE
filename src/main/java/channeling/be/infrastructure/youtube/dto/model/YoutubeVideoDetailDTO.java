package channeling.be.infrastructure.youtube.dto.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class YoutubeVideoDetailDTO {
	private final String description;
	private String categoryId;
	private final Long viewCount;
	private final Long likeCount;
	private final Long commentCount;

	public void updateCategoryId(String categoryId){
		this.categoryId=categoryId;
	}
}
