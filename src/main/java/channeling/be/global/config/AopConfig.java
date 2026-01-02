package channeling.be.global.config;

import channeling.be.global.aop.LogTraceAspect;
import channeling.be.infrastructure.log.LogTrace;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfig {
    @Bean
    public LogTraceAspect logTraceAspect(LogTrace logTrace) {
        return new LogTraceAspect(logTrace);
    }
    @Bean
    public LogTrace logTrace() {
        return new LogTrace();
    }
}
