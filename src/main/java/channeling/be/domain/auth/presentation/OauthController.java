package channeling.be.domain.auth.presentation;

import channeling.be.domain.auth.annotation.LoginMember;
import channeling.be.domain.auth.application.OauthService;
import channeling.be.domain.member.domain.Member;
import channeling.be.response.exception.handler.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class OauthController {
    private final OauthService oauthService;

    @PostMapping("/withdrawal")
    public ApiResponse<String> withdrawal(@LoginMember Member member, @RequestBody OauthReq.withdrawal request ) {
        return ApiResponse.onSuccess(oauthService.withdrawMember(member,request));
    }
    @PostMapping("/resign_up")
    public ApiResponse<String> withdrawal(@RequestBody OauthReq.resignation request ) {
        return ApiResponse.onSuccess(oauthService.resignMember(request));
    }
}
