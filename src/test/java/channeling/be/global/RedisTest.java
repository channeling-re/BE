package channeling.be.global;

import channeling.be.infrastructure.redis.RedisUtil;
import channeling.be.infrastructure.youtube.application.YoutubeService;
import channeling.be.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class RedisTest extends IntegrationTestSupport {

    @Autowired
    private RedisUtil redisUtil;


    @Test
    void redisSetAndGetTest() {
        // given
        String key = "testKey";
        String value = "testValue";

        // when
        redisUtil.setData(key, value);

        // then
        String retrievedValue = redisUtil.getData(key);
        assertThat(retrievedValue).isEqualTo(value);
    }
}
