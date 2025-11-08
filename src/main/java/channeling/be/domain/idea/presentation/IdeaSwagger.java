package channeling.be.domain.idea.presentation;

import channeling.be.global.auth.annotation.LoginMember;
import channeling.be.domain.member.domain.Member;
import channeling.be.response.exception.handler.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "아이디어 API", description = "아이디어 관련 API입니다.")
public interface IdeaSwagger{

    @Operation(summary = "아이디어 북마크 추가 혹은 제거", description = "아이디어를 북마크 혹은 북마크 제거를 합니다.")
    @PatchMapping("/{idea-id}/bookmarks")
    ApiResponse<IdeaResDto.ChangeIdeaBookmarkRes> changeIdeaBookmark(
            @Parameter(description = "북마크 변경 요청할 아이디어 아이디 (북마크한 아이디어 리스트 조회 시, 응답에 포함 되어 있습니다.)", example = "1")
            @PathVariable("idea-id") Long ideaId,
            @Parameter(hidden = true)
            @LoginMember Member loginMember);


    @Operation(summary = "북마크한 아이디어 리스트 조회", description = "북마크한 아이디어를 페이지네이션을 사용하여 조회합니다.")
    @GetMapping("/bookmarks")
   ApiResponse<IdeaResDto.GetBookmarkedIdeaListRes> GetBookmarkedIdeaList(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수", example = "6")
            @RequestParam(value = "size", defaultValue = "6") int size,
            @Parameter(hidden = true)
            @LoginMember Member loginMember);
}
