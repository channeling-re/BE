package channeling.be.infrastructure.kafka;

import channeling.be.global.config.KafkaConfig;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, VideoSyncRequestDto> kafkaTemplate;

    public void sendVideoSyncRequest(VideoSyncRequestDto requestDto) {
        log.info("Kafka 토픽 '{}'으로 비디오 동기화 요청 메시지를 전송합니다.", KafkaConfig.VIDEO_SYNC_TOPIC);
        kafkaTemplate.send(KafkaConfig.VIDEO_SYNC_TOPIC, requestDto);
    }
}