package channeling.be.domain.idea.presentation;

import channeling.be.global.auth.annotation.LoginMember;
import channeling.be.domain.idea.application.IdeaService;
import channeling.be.domain.member.domain.Member;
import channeling.be.response.exception.handler.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ideas")
public class IdeaController implements IdeaSwagger{
    private final IdeaService ideaService;

    @Override
    @PatchMapping("/{idea-id}/bookmarks")
    public ApiResponse<IdeaResDto.ChangeIdeaBookmarkRes> changeIdeaBookmark(@PathVariable("idea-id") Long ideaId,
                                                                        @LoginMember Member loginMember) {
        return ApiResponse.onSuccess(ideaService.changeIdeaBookmark(ideaId, loginMember));
    }

    @GetMapping("/bookmarks")
    public ApiResponse<IdeaResDto.GetBookmarkedIdeaListRes> GetBookmarkedIdeaList(
                    @RequestParam(value = "page", defaultValue = "1") int page,
                    @RequestParam(value = "size", defaultValue = "6") int size,
                    @LoginMember Member loginMember) {

        return ApiResponse.onSuccess(ideaService.getBookmarkedIdeaList(loginMember,page,size));
    }

}
