package channeling.be.global.interceptor;

import channeling.be.infrastructure.log.LogTrace;
import channeling.be.infrastructure.log.TraceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

@RequiredArgsConstructor
public class RestTemplateLoggingInterceptor
        implements ClientHttpRequestInterceptor {

    private final LogTrace logTrace;

    @Override
    public ClientHttpResponse intercept(
            HttpRequest request,
            byte[] body,
            ClientHttpRequestExecution execution) throws IOException {

        TraceStatus status = null;
        try {
            String message = "HTTP OUT " + request.getMethod() + " " + request.getURI();
            status = logTrace.begin(message);
            ClientHttpResponse response = execution.execute(request, body);
            logTrace.end(status);
            return response;
        } catch (Exception e) {
            logTrace.exception(status, e);
            throw e;
        }
    }
}