package channeling.be.domain.auth.presentation;

import channeling.be.domain.auth.application.OauthService;
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

    private final OauthService oauthService;

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestBody OauthReq.Logout request) {
        return ApiResponse.onSuccess(oauthService.logout(request.getRefreshToken()));
    }
}
