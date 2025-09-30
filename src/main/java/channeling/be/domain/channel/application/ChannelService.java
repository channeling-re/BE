package channeling.be.domain.channel.application;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.member.domain.Member;
import channeling.be.response.exception.handler.ChannelHandler;

import static channeling.be.domain.channel.presentation.dto.request.ChannelRequestDto.*;

public interface ChannelService {
	/**
	 * 채널의 컨셉 정보를 수정합니다.
	 */
	Channel editChannelConcept(Long channelId, EditChannelConceptReqDto request, Member member);
	/**
	 * 채널 ID로 채널의 존재 여부를 검증합니다.
	 *
	 * @param channelId 채널 ID
	 * @throws ChannelHandler 채널이 존재하지 않을 경우 예외 발생
	 */
	void validateChannelByIdAndMember(Long channelId,Member member);
	/**
	 * 채널의 타겟 정보를 수정합니다.
	 */
	Channel editChannelTarget(Long channelId, EditChannelTargetReqDto request, Member member);

	/**
	 * 멤버에 해당하는 채널을 찾거나 생성합니다.
	 *
	 * @param member 멤버 객체
	 * @return 채널 객체
	 */
	Channel updateOrCreateChannelByMember(channeling.be.domain.member.domain.Member member);
	/*
	 * 채널 정보를 조회합니다.
	 */
	Channel getChannel(Long channelId, Member member);

}
