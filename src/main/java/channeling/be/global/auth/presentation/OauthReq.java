package channeling.be.global.auth.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public class OauthReq {
    @Getter
    public static class ReIssueToken{
        @NotNull(message = "입력 토큰은 null일 수 없습니다.") // null 방지
        @Schema(type = "string", description = "로그인시 받았던 리프레시 토큰")
        String refreshToken; //리프레시 토큰
    }

    @Getter
    public static class Logout{
        @NotNull(message = "입력 토큰은 null일 수 없습니다.") // null 방지
        @Schema(type = "string", description = "로그인시 받았던 리프레시 토큰")
        String refreshToken; //리프레시 토큰
    }
    @Getter
    public static class withdrawal {
        @NotNull(message = "입력 토큰은 null일 수 없습니다.") // null 방지
        @Schema(type = "string", description = "로그인시 받았던 엑세스 토큰")
        String accessToken; //리프레시 토큰
        @NotNull(message = "입력 토큰은 null일 수 없습니다.") // null 방지
        @Schema(type = "string", description = "로그인시 받았던 리프레시 토큰")
        String refreshToken; //리프레시 토큰
    }

    @Getter
    public static class resignation {
        @NotNull(message = "입력 토큰은 null일 수 없습니다.") // null 방지
        @Schema(type = "string", description = "로그인시 받았던 임시 토큰")
        String tempToken; //임시 토큰
    }
}
