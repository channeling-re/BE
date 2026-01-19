package channeling.be.support;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@TestConfiguration
public class WireMockFastApiTestSupport {

    private static WireMockServer wireMockServer;

    @Bean
    public WireMockServer wireMockServer() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(
                    WireMockConfiguration.options()
                            .port(8000)
            );
            wireMockServer.start();

            wireMockServer.stubFor(
                    post(urlPathEqualTo("/reports/v2"))
                            .willReturn(
                                    aResponse()
                                            .withStatus(200)
                                            .withHeader("Content-Type", "application/json")
                                            .withBodyFile("fastapi/create-report-success.json")
                            )
            );

            wireMockServer.addMockServiceRequestListener((req, res) -> {
                System.out.println("🟢 WireMock hit: " + req.getMethod() + " " + req.getUrl());
            });
            System.out.println("✅ WireMock started on port 8000");
        }
        return wireMockServer;
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add(
                "app.report.url",
                () -> "http://localhost:8000/reports"
        );
    }

    @PreDestroy
    public void stopWireMock() {
        if (wireMockServer != null && wireMockServer.isRunning()) {
            wireMockServer.stop();
        }
    }
}
