package channeling.be.domain.channel.presentation;

import channeling.be.global.auth.annotation.LoginMember;
import channeling.be.domain.channel.presentation.dto.request.ChannelRequestDto;
import channeling.be.domain.channel.presentation.dto.response.ChannelResponseDto;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.domain.VideoType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import channeling.be.response.exception.handler.ApiResponse;

import org.springframework.web.bind.annotation.*;

@Tag(name = "채널 API", description = "채널 관련 API입니다.")
public interface ChannelSwagger {


	@Operation(summary = "채널의 비디오 목록 조회", description = "채널의 영상 목록을 페이지네이션으로 조회합니다.")
	@GetMapping("/{channel-id}/videos")
	ApiResponse<ChannelResDTO.ChannelVideoList> getChannelVideos(
		@Parameter(hidden = true)
		@LoginMember Member member,
		@Parameter(description = "요청 채널 아이디 (로그인 성공 시 바디에 포함 되어 있습니다.)", example = "1")
		@PathVariable("channel-id") Long channelId,
		@Parameter(description = "Long,Short 여부")
		@RequestParam(value = "type") VideoType type,
		@Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
		@RequestParam(defaultValue = "1") int page,
		@Parameter(description = "페이지당 항목 수", example = "8")
		@RequestParam(defaultValue = "8") int size);


	@GetMapping("/{channel-id}/reports")
	@Operation(summary = "채널의 레포트 조회 API (page)", description = "특정 채널의 레포트 리스트를 페이지를 통해 조회합니다.")
	ApiResponse<ChannelResDTO.ChannelReportList> getReports(
		@Parameter(hidden = true)
		@LoginMember Member member,
		@Parameter(description = "요청 채널 아이디 (로그인 성공 시 바디에 포함 되어 있습니다.)", example = "1")
		@PathVariable("channel-id") Long channelId,
		@Parameter(description = "Long,Short 여부")
		@RequestParam(value = "type") VideoType type,
		@Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
		@RequestParam(value = "page", defaultValue = "1") int page,
		@Parameter(description = "페이지당 항목 수", example = "8")
		@RequestParam(value = "size", defaultValue = "8") int size);


	@Operation(summary = "채널의 컨셉 수정", description = "채널의 컨셉을 수정합니다.")
        @PatchMapping("/{channel-id}/concepts")
        ApiResponse<ChannelResponseDto.EditChannelConceptResDto> editChannelConcept(
                @Parameter(description = "요청 채널 아이디 (로그인 성공 시 바디에 포함 되어 있습니다.)", example = "1")
                @PathVariable("channel-id") Long channelId,
                @RequestBody ChannelRequestDto.EditChannelConceptReqDto request,
                @Parameter(hidden = true)
                @LoginMember Member member);


        @Operation(summary = "채널의 타겟 수정", description = "채널의 타겟을 수정합니다.")
        @PatchMapping("/{channel-id}/targets")
        ApiResponse<ChannelResponseDto.EditChannelTargetResDto> editChannelTarget(
                @Parameter(description = "요청 채널 아이디 (로그인 성공 시 바디에 포함 되어 있습니다.)", example = "1")
                @PathVariable("channel-id") Long channelId,
                @RequestBody ChannelRequestDto.EditChannelTargetReqDto request,
                @Parameter(hidden = true)
                @LoginMember Member member);



        @Operation(summary = "채널의 대시보드 정보", description = "채널의 대시보드 정보를 조회합니다. 채널 정보 페이지의 상단의 채널 관련 정보를 반환합니다..")
        @GetMapping("{channel-id}")
        ApiResponse<ChannelResDTO.ChannelInfo> getChannel(
                @Parameter(description = "요청 채널 아이디 (로그인 성공 시 바디에 포함 되어 있습니다.)", example = "1")
                @PathVariable("channel-id") Long channelId,
                @Parameter(hidden = true)
                @LoginMember Member loginMember);

        @Operation(summary = "채널 추천 영상 조회 API", description = "(홈화면) 자신의 채널의 추천 영상을 조회합니다.")
        @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
                @io.swagger.v3.oas.annotations.responses.ApiResponse(
                        responseCode = "200",
                        description = "성공입니다.",
                        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ChannelResDTO.PageDto.class))}
                )
        })
        ApiResponse<ChannelResDTO.PageDto> getRecommendedChannelVideos(
                @PathVariable("channel-id") Long channelId,
                @RequestParam(value = "page", defaultValue = "1") Integer page,
                @RequestParam(value = "size", defaultValue = "10") Integer size,
                @Parameter(hidden = true) @LoginMember Member loginMember);
}


