package channeling.be.support;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * 모든 통합 테스트에서 상속받는 추상 클래스입니다.
 * @Testcontainers를 사용하여 테스트 실행 전 Docker Compose로 Kafka와 Redis 컨테이너를 실행하고,
 * 테스트가 끝나면 자동으로 종료합니다.
 */
@ActiveProfiles({"test", "jwt"})
@Testcontainers
public abstract class IntegrationTestSupport {

    // docker-compose.yml에 정의된 서비스 이름과 포트
    private static final String REDIS_SERVICE_NAME = "redis-test";
    private static final int REDIS_PORT = 6379;
    private static final String KAFKA_SERVICE_NAME = "kafka-test";
    private static final int KAFKA_PORT = 9092;

    @Container
    public static final DockerComposeContainer<?> composeContainer =
            new DockerComposeContainer<>(new File("src/test/resources/docker-compose.test.yml"))
                    // redis-test 서비스가 6379 포트를 리스닝할 때까지 대기
                    .withExposedService(REDIS_SERVICE_NAME, REDIS_PORT, Wait.forListeningPort())
                    // kafka-test 서비스가 9092 포트를 리스닝할 때까지 대기
                    .withExposedService(KAFKA_SERVICE_NAME, KAFKA_PORT, Wait.forListeningPort());

    /**
     * @DynamicPropertySource를 사용하여 컨테이너가 실행된 후 동적으로 할당된
     * 호스트와 포트 정보를 Spring의 테스트 프로퍼티에 주입합니다.
     */
    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        String redisHost = composeContainer.getServiceHost(REDIS_SERVICE_NAME, REDIS_PORT);
        Integer redisPort = composeContainer.getServicePort(REDIS_SERVICE_NAME, REDIS_PORT);

        String kafkaHost = composeContainer.getServiceHost(KAFKA_SERVICE_NAME, KAFKA_PORT);
        Integer kafkaPort = composeContainer.getServicePort(KAFKA_SERVICE_NAME, KAFKA_PORT);

        // Spring Boot 설정의 Redis 및 Kafka 호스트/포트를 동적으로 설정
        registry.add("spring.data.redis.host", () -> redisHost);
        registry.add("spring.data.redis.port", () -> redisPort);
        registry.add("spring.kafka.bootstrap-servers", () -> String.format("%s:%d", kafkaHost, kafkaPort));
    }

    protected Object awaitOneRecordValue(Consumer<String, Object> consumer, String topic, Duration timeout) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            ConsumerRecords<String, Object> records = consumer.poll(Duration.ofMillis(300));
            for (ConsumerRecord<String, Object> rec : records.records(topic)) {
                return rec.value();
            }
        }
        fail("타임아웃 내에 토픽(" + topic + ")에서 메시지를 수신하지 못했습니다.");
        return null; // unreachable
    }
}
