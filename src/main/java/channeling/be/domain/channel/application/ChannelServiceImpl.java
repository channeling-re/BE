package channeling.be.domain.channel.application;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.channel.domain.repository.ChannelRepository;
import channeling.be.domain.channel.presentation.converter.ChannelConverter;
import channeling.be.domain.channel.presentation.dto.request.ChannelRequestDto;
import channeling.be.domain.member.domain.Member;
import channeling.be.infrastructure.kafka.dto.KafkaVideoSyncDto;
import channeling.be.infrastructure.kafka.producer.KafkaProducerService;
import channeling.be.infrastructure.redis.RedisUtil;
import channeling.be.infrastructure.youtube.application.YoutubeService;
import channeling.be.infrastructure.youtube.presentation.YoutubeDto;
import channeling.be.infrastructure.youtube.res.YoutubeChannelResDTO;
import channeling.be.response.exception.handler.ChannelHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static channeling.be.response.code.status.ErrorStatus._CHANNEL_NOT_FOUND;
import static channeling.be.response.code.status.ErrorStatus._CHANNEL_NOT_MEMBER;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
@Service
public class ChannelServiceImpl implements ChannelService {
    private final ChannelRepository channelRepository;
    private final YoutubeService youtubeService;
    private final RedisUtil redisUtil;
    private final KafkaProducerService kafkaProducerService;


    @AllArgsConstructor
    @Getter
    public static class YoutubeChannelVideoData {
        YoutubeChannelResDTO.Item item;
        List<YoutubeDto.VideoBriefDTO> briefs;
        List<YoutubeDto.VideoDetailDTO> details;
    }


    @Override
    public Channel getChannel(Long channelId, Member loggedInMember) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelHandler(_CHANNEL_NOT_FOUND));

        if (!channel.getMember().getId().equals(loggedInMember.getId())) {
            throw new ChannelHandler(_CHANNEL_NOT_MEMBER);
        }

        return channel;
    }

    @Override
    @Transactional
    public Channel editChannelConcept(Long channelId, ChannelRequestDto.EditChannelConceptReqDto request,Member loginMember) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelHandler(_CHANNEL_NOT_FOUND));

        if (!channel.getMember().getId().equals(loginMember.getId())) {
            throw new ChannelHandler(_CHANNEL_NOT_MEMBER);
        }
        channel.editConcept(request.getConcept()); // 더티체킹
        return channel;
    }

    @Override
    public void validateChannelByIdAndMember(Long channelId,Member member) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelHandler(_CHANNEL_NOT_FOUND));

        if (!channel.getMember().getId().equals(member.getId())) {
            throw new ChannelHandler(_CHANNEL_NOT_MEMBER);
        }
    }

    @Override
    @Transactional
    public Channel editChannelTarget(Long channelId, ChannelRequestDto.EditChannelTargetReqDto request, Member loginMember) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelHandler(_CHANNEL_NOT_FOUND));

        if (!channel.getMember().getId().equals(loginMember.getId())) {
            throw new ChannelHandler(_CHANNEL_NOT_MEMBER);
        }
        channel.editTarget(request.getTarget()); // 더티체킹
        return channel;
    }


    @Override
    @Transactional
    public Channel updateOrCreateChannelByMember(Member member) {

        String googleAccessToken = redisUtil.getGoogleAccessToken(member.getId());

        // 유튜브 채널 정보 가져오기
        YoutubeChannelResDTO.Item item = youtubeService.syncChannel(googleAccessToken);

        // 채널 없으면 생성, 있으면 업데이트
        Channel channel = channelRepository.findByMember(member)
                .map(existingChannel -> {
                    existingChannel.updateByYoutube(item);
                    return existingChannel;
                })
                .orElseGet(() -> channelRepository.save(ChannelConverter.toNewChannel(item, member)));

        // 채널 생성/업데이트 메시지 발행
        kafkaProducerService.sendVideoSyncRequest(new KafkaVideoSyncDto(item, googleAccessToken, channel));

        return channel;
    }

}

