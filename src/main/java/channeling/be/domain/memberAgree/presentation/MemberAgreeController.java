package channeling.be.domain.memberAgree.presentation;

import channeling.be.global.auth.annotation.LoginMember;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.memberAgree.application.MemberAgreeService;
import channeling.be.response.exception.handler.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/member-agree")
public class MemberAgreeController implements MemberAgreeApi {

    private final MemberAgreeService memberAgreeService;

    @Override
    @PatchMapping("")
    public ApiResponse<MemberAgreeResDto.Edit> editMemberAgree(@Valid @RequestBody MemberAgreeReqDto.Edit dto,
                                          @LoginMember Member member) {
        return ApiResponse.onSuccess(
            MemberAgreeConverter.toEditMemberAgreeResDto(
                memberAgreeService.editMemberAgree(dto, member)
            )
        );
    }
}
