package channeling.be.infrastructure.youtube;

import channeling.be.domain.channel.event.ChannelEvent;
import channeling.be.infrastructure.youtube.application.YoutubeService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class YoutubeEventListener {
    private final YoutubeService youtubeService;

    @Async
    @TransactionalEventListener
    public void handleMemberLoggedInEvent(ChannelEvent event) {
        youtubeService.syncVideos(event.getItem(), event.getAccessToken(), event.getChannel());
    }
}
