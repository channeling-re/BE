package channeling.be.domain.video.presentaion;

import channeling.be.global.auth.annotation.LoginMember;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.application.VideoService;
import channeling.be.response.exception.handler.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/videos")
public class VideoController implements VideoSwagger {

    private final VideoService videoService;

    @Override
    @GetMapping("/{video-id}")
    public ApiResponse<VideoResDTO.ReportVideoInfo> getVideoInfo(Long videoId, @LoginMember Member loginMember) {
        return ApiResponse.onSuccess(VideoResDTO.ReportVideoInfo.from(videoService.checkVideoWithMember(videoId, loginMember)));
    }
}
