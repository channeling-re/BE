package channeling.be.global.config;

import java.io.IOException;
import java.net.HttpURLConnection;

import channeling.be.global.interceptor.RestTemplateLoggingInterceptor;
import channeling.be.infrastructure.log.LogTrace;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final LogTrace logTrace;


    @Bean
	public RestTemplate googleRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);

        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors()
                .add(new RestTemplateLoggingInterceptor(logTrace));
        return restTemplate;
	}
	@Bean("noRedirectRestTemplate")
	public RestTemplate noRedirectRestTemplate(){

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory() {
                @Override
                protected void prepareConnection(
                        HttpURLConnection connection,
                        String httpMethod) throws IOException {
                    super.prepareConnection(connection, httpMethod);
                    connection.setInstanceFollowRedirects(false);
                }
            };
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(5000);

            RestTemplate restTemplate = new RestTemplate(factory);
            restTemplate.getInterceptors()
                    .add(new RestTemplateLoggingInterceptor(logTrace));
            return restTemplate;
	}
}
