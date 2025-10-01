package channeling.be.domain.auth.presentation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

public class OauthReq {
    @Getter
    public static class Logout{
        @NotNull(message = "입력 토큰은 null일 수 없습니다.") // null 방지
        @Schema(type = "string", description = "로그인시 받았던 리프레시 토큰")
        String refreshToken; //리프레시 토큰
    }
}
