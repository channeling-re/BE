package channeling.be.infrastructure.youtube;

import channeling.be.domain.channel.event.ChannelEvent;
import channeling.be.infrastructure.youtube.application.YoutubeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class YoutubeEventListener {
    private final YoutubeUtil youtubeUtil;

    @Async
    @TransactionalEventListener
    public void handleMemberLoggedInEvent(ChannelEvent event) {
        youtubeUtil.syncVideos(event.getItem(), event.getAccessToken(), event.getChannel());
    }
}
