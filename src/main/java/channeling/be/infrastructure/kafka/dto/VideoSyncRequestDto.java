package channeling.be.infrastructure.kafka.dto;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.infrastructure.youtube.dto.res.YoutubeChannelResDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VideoSyncRequestDto {
    private YoutubeChannelResDTO.Item item;
    private String accessToken;
    private Channel channel;
}
