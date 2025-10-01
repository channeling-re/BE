package channeling.be.domain.auth.presentation;

import channeling.be.global.infrastructure.jwt.JwtUtil;
import channeling.be.response.exception.handler.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OauthController {
    private final JwtUtil jwtUtil;

    @PostMapping("/reissue")
    public ApiResponse<OauthRes.ReIssueToken> reIssueToken(@RequestBody OauthReq.ReIssueToken request) {
        String reissuedAccessToken = jwtUtil.reissueAccessToken(request.refreshToken);
        return ApiResponse.onSuccess(new OauthRes.ReIssueToken(reissuedAccessToken));
    }

}
