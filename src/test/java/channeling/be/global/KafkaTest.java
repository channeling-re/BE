package channeling.be.global;

import channeling.be.domain.channel.domain.Channel;
import channeling.be.domain.member.domain.Member;
import channeling.be.infrastructure.kafka.dto.KafkaVideoSyncDto;
import channeling.be.infrastructure.kafka.producer.KafkaProducerService;
import channeling.be.infrastructure.youtube.application.YoutubeService;
import channeling.be.infrastructure.youtube.res.YoutubeChannelResDTO;
import channeling.be.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

/**
 * Kafka 통합 테스트
 * Testcontainers로 실행된 Kafka 컨테이너와의 실제 메시지 발행(Produce)-소비(Consume) 과정을 검증합니다.
 */
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class KafkaTest extends IntegrationTestSupport {

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @MockitoBean
    private YoutubeService youtubeService;

    @DisplayName("채널 동기화 요청 메시지를 Kafka에 발행하면, Consumer가 이를 수신하여 관련 로직을 수행한다.")
    @Test
    void producerAndConsumerIntegrationTest() {
        // given: 테스트용 Member와 Channel, 그리고 Kafka 메시지 DTO를 생성합니다.
        Member member = Member.builder().id(1L).nickname("testuser").googleEmail("test@google.com").build();
        Channel channel = Channel.builder().id(1L).member(member).name("testchannel").youtubeChannelId("UC-test").joinDate(LocalDateTime.now()).channelUpdateAt(LocalDateTime.now()).build();
        KafkaVideoSyncDto dto = new KafkaVideoSyncDto(new YoutubeChannelResDTO.Item(), "test-access-token", channel);

        // when: Kafka Producer가 메시지를 토픽에 발행합니다.
        kafkaProducerService.sendVideoSyncRequest(dto);

        // then: Consumer가 메시지를 비동기적으로 처리할 때까지 최대 5초간 기다립니다.
        // Consumer의 로직 마지막에 호출되는 youtubeService.syncVideos 메소드가 1번 호출되었는지 검증합니다.
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
            verify(youtubeService).syncVideos(any(), any(String.class), any(Channel.class));
        });
    }
}
