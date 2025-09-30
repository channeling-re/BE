package channeling.be.infrastructure.kafka;

import channeling.be.global.config.KafkaConfig;
import channeling.be.infrastructure.youtube.application.YoutubeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final YoutubeService youtubeService;

    @KafkaListener(topics = KafkaConfig.VIDEO_SYNC_TOPIC, groupId = "be-youtube-group")
    public void consumeVideoSyncRequest(VideoSyncRequestDto requestDto) {
        log.info("Kafka 토픽에서 채널 ID '{}'의 비디오 동기화 요청을 수신했습니다.", requestDto.getChannel().getId());
        try {
            youtubeService.syncVideos(requestDto.getItem(), requestDto.getAccessToken(), requestDto.getChannel());
            log.info("채널 ID '{}'의 비디오 동기화를 성공적으로 완료했습니다.", requestDto.getChannel().getId());
        } catch (Exception e) {
            log.error("채널 ID '{}'의 비디오 동기화 중 에러가 발생했습니다.", requestDto.getChannel().getId(), e);
            // KafkaConfig 설정을 통해 Dead Letter Queue(DLQ) 토픽으로 전송
        }
    }
}