package channeling.be.domain.channel.event;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.infrastructure.youtube.dto.res.YoutubeChannelResDTO;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ChannelEvent {
    private final YoutubeChannelResDTO.Item item;
    private final String accessToken;
    private final Channel channel;
}
