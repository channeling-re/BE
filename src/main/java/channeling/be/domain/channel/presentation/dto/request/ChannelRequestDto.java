package channeling.be.domain.channel.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

public class ChannelRequestDto {
    @Setter
    @Getter
    public static class EditChannelConceptReqDto{
        @NotNull(message = "채널 컨셉은 null일 수 없습니다.") // null 방지
        @Size(max = 500, message = "채널 컨셉은 최대 500자까지 입력할 수 있습니다.")
        @Schema(type = "string", description = "수정할 채널의 컨셉입니다", example = "기술 리뷰")
        String concept; // 수정할 컨셉 정보
    }

    @Getter
    public static class EditChannelTargetReqDto{
        @NotNull(message = "채널 타겟은 null일 수 없습니다.") // null 방지
        @Size(max = 100, message = "채널 타겟은 최대 100자까지 입력할 수 있습니다.")
        @Schema(type = "string", description = "수정할 채널의 타겟입니다", example = "20대 대학생")
        String target; // 수정할 타겟 정보
    }
}
