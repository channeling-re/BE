package channeling.be.domain.auth.application;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GoogleOAuthService {
    private final RestTemplate googleRestTemplate;

    public GoogleOAuthService(@Qualifier("googleRestTemplate") RestTemplate restTemplate) {
        this.googleRestTemplate = restTemplate;
    }


    @Value("${GOOGLE_CLIENT_ID:default-client-id}")
    private String clientId;

    @Value("${GOOGLE_CLIENT_SECRET:default-client-secret}")
    private String clientSecret;


    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";



    /**
     * 구글 리프레시 토큰을 사용하여 새 액세스 토큰을 요청합니다.
     *
     * @param refreshToken 발급받은 구글 리프레시 토큰
     * @return 새로 발급된 액세스 토큰
     */
    public String refreshAccessToken(String refreshToken) {

        System.out.println("Using googleRestTemplate: " + googleRestTemplate);


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&refresh_token=" + refreshToken
                + "&grant_type=refresh_token";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = googleRestTemplate.exchange(
                TOKEN_URI,
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to refresh Google access token: " + response.getStatusCode());
        }

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("access_token")) {
            throw new RuntimeException("No access token found in Google response");
        }

        return (String) responseBody.get("access_token");
    }

}
