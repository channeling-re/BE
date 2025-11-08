package channeling.be.global.auth.presentation;

import channeling.be.domain.member.domain.Member;
import channeling.be.global.auth.annotation.LoginMember;
import channeling.be.global.auth.application.OauthService;
import channeling.be.infrastructure.jwt.JwtUtil;
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
    private final OauthService oauthService;


    @PostMapping("/reissue")
    public ApiResponse<OauthRes.ReIssueToken> reIssueToken(@RequestBody OauthReq.ReIssueToken request) {
        String reissuedAccessToken = jwtUtil.reissueAccessToken(request.refreshToken);
        return ApiResponse.onSuccess(new OauthRes.ReIssueToken(reissuedAccessToken));
    }

    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestBody OauthReq.Logout request) {
        return ApiResponse.onSuccess(oauthService.logout(request.getRefreshToken()));
    }

    @PostMapping("/withdrawal")
    public ApiResponse<String> withdrawal(@LoginMember Member member, @RequestBody OauthReq.withdrawal request ) {
        return ApiResponse.onSuccess(oauthService.withdrawMember(member,request));
    }
    @PostMapping("/resign_up")
    public ApiResponse<String> withdrawal(@RequestBody OauthReq.resignation request ) {
        return ApiResponse.onSuccess(oauthService.resignMember(request));
    }
}
