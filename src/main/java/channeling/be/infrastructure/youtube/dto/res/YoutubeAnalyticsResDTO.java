package channeling.be.infrastructure.youtube.dto.res;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class YoutubeAnalyticsResDTO {
	private List<ColumnHeader> columnHeaders;
	private List<List<Object>> rows;

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ColumnHeader {
		private String name;
		private String columnType;
		private String dataType;
	}
}
