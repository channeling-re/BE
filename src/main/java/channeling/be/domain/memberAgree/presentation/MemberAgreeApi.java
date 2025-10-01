package channeling.be.domain.memberAgree.presentation;

import channeling.be.global.auth.annotation.LoginMember;
import channeling.be.domain.member.domain.Member;
import channeling.be.response.exception.handler.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "회원 동의 API", description = "회원 동의 관련 API입니다.")
public interface MemberAgreeApi {

    @Operation(summary = "회원 동의 수정 API", description = "회원 동의를 수정합니다. (토큰 필수)")
    @PatchMapping("")
    ApiResponse<MemberAgreeResDto.Edit> editMemberAgree(
            @Parameter(description = "질문별 동의 여부")
            @RequestBody @Valid MemberAgreeReqDto.Edit dto,
            @Parameter(hidden = true)
            @LoginMember Member member);
}
