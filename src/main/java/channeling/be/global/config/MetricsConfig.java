package channeling.be.global.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    @Bean
    public Counter helloCounter(MeterRegistry registry) {
        return Counter.builder("http_hello_total")
                .description("hello endpoint 호출 수")
                .tag("version", "v1")
                .register(registry);
    }
}
