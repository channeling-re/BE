package channeling.be.domain.channel.presentation.converter;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.member.domain.Member;
import channeling.be.infrastructure.youtube.res.YoutubeChannelResDTO;
import lombok.extern.slf4j.Slf4j;

import static channeling.be.domain.channel.presentation.dto.response.ChannelResponseDto.*;

import java.time.LocalDateTime;

@Slf4j
public class ChannelConverter {
    public static EditChannelConceptResDto toEditChannelConceptResDto(Channel channel) {
        return EditChannelConceptResDto.builder()
                .channelId(channel.getId())
                .updatedConcept(channel.getConcept())
                .build();
    }
    public static EditChannelTargetResDto toEditChannelTargetResDto(Channel channel) {
        return EditChannelTargetResDto.builder()
                .channelId(channel.getId())
                .updatedTarget(channel.getTarget())
                .build();
    }

    // Member 가입시 채널 생성용 (기본값 null)
    public static Channel toNewChannel(YoutubeChannelResDTO.Item item, Member member) {
        return Channel.builder()
                .name(item.getSnippet().getTitle())
                .youtubeChannelId(item.getId())
                .youtubePlaylistId(item.getContentDetails().getRelatedPlaylists().getUploads())
                .image(item.getSnippet().getThumbnails().getDefaultThumbnail().getUrl())
                .link("https://www.youtube.com/channel/" + item.getId())
                .joinDate(item.getSnippet().getPublishedAt())
                .view(item.getStatistics().getViewCount())
                .subscribe(item.getStatistics().getSubscriberCount())
                .videoCount(item.getStatistics().getVideoCount())
                .member(member)
                .target(null)
                .concept(null)
                .likeCount(null)
                .comment(null)
                .share(null)
                .channelHashTag(null)
                .channelUpdateAt(LocalDateTime.now())
                .build();
    }

}
