package channeling.be.global.config;

import channeling.be.infrastructure.kafka.dto.VideoSyncRequestDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@RequiredArgsConstructor
@Configuration
public class KafkaConfig {

    public static final String VIDEO_SYNC_TOPIC = "video-sync-topic";
    public static final String VIDEO_SYNC_DLT = ".dlt";


    /**
     * Dead Letter Topic으로 메시지를 보낼 때 사용할 KafkaTemplate
     */
    private final KafkaTemplate<String, VideoSyncRequestDto> kafkaTemplate;

    /**
     * 재시도 및 DLT 처리
     */
    @Bean
    public DefaultErrorHandler errorHandler() {
        // 1. DeadLetterPublishingRecoverer: 재시도에 모두 실패한 메시지를 DLT 전송함
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (consumerRecord, exception) -> new TopicPartition(consumerRecord.topic() + VIDEO_SYNC_DLT, -1));

        // 2. FixedBackOff: 재시도 간격 설정
        var backOff = new FixedBackOff(1000L, 2);

        // 3. DefaultErrorHandler: 위에서 만든 recoverer와 backOff를 사용하여 에러 핸들러 생성
        return new DefaultErrorHandler(recoverer, backOff);
    }

    /**
     * 위에서 만든 Error Handler를 Kafka Listener에 적용하기 위한 Container Factory 설정
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VideoSyncRequestDto> kafkaListenerContainerFactory(
            ConsumerFactory<String, VideoSyncRequestDto> consumerFactory,
            DefaultErrorHandler errorHandler) {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, VideoSyncRequestDto>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }
}