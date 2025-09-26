package channeling.be.domain.channel.presentation.converter;

import channeling.be.domain.channel.application.model.Stats;
import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.member.domain.Member;
import channeling.be.domain.video.domain.VideoCategory;
import channeling.be.infrastructure.youtube.dto.res.YoutubeChannelResDTO;
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

    public static void updateChannel(Channel channel, YoutubeChannelResDTO.Item item,String topCategoryId ,Stats stats,long shares) {
        channel.updateChannelInfo(
            item.getSnippet().getTitle(),
            item.getId(),
            item.getContentDetails().getRelatedPlaylists().getUploads(),
            item.getSnippet().getThumbnails().getDefaultThumbnail().getUrl(),
            "https://www.youtube.com/channel/" + item.getId(),
            item.getSnippet().getPublishedAt(),
            item.getStatistics().getViewCount(),
            item.getStatistics().getSubscriberCount(),
            item.getStatistics().getVideoCount(),
            stats.likeCount(),
            stats.commentCount(),
            topCategoryId,
            shares
        );
        channel.updateChannelStats(stats.likeCount(), stats.commentCount());
    }

    public static Channel toNewChannel(YoutubeChannelResDTO.Item item, Member member,long shares,String topCategoryId) {
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
            .target("default")
            .likeCount(0L)
            .channelHashTag(VideoCategory.ofId(topCategoryId))
            .channelUpdateAt(LocalDateTime.now())
            .concept("default")
            .comment(0L)
            .share(shares)
            .build();
    }

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
