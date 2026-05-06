package common.extensions;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import common.annotations.FraudCheckMock;
import org.junit.jupiter.api.extension.*;

public class FraudCheckWireMockExtension
    implements BeforeEachCallback, BeforeAllCallback, AfterAllCallback {
  private static WireMockServer wireMockServer;
  private static final Object LOCK = new Object();
  private static final int WIREMOCK_PORT = 8080;

  @Override
  public void beforeAll(ExtensionContext context) {
    synchronized (LOCK) {
      if (wireMockServer == null) {
        wireMockServer =
            new WireMockServer(
                WireMockConfiguration.wireMockConfig()
                    .port(WIREMOCK_PORT)
                    .bindAddress("0.0.0.0")); // важно для доступа из контейнера
        wireMockServer.start();
      }
    }
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    FraudCheckMock mockConfig =
        context
            .getTestMethod()
            .map(method -> method.getAnnotation(FraudCheckMock.class))
            .orElseGet(
                () ->
                    context
                        .getTestClass()
                        .map(clazz -> clazz.getAnnotation(FraudCheckMock.class))
                        .orElse(null));
    if (mockConfig == null) return;

    synchronized (LOCK) {
      // Очищаем старые стубы перед настройкой новых
      wireMockServer.resetAll();
      // Регистрируем стубы для текущего теста
      setupStubs(mockConfig);
    }
  }

  private void setupStubs(FraudCheckMock config) {
    WireMock wireMock = new WireMock("0.0.0.0", WIREMOCK_PORT);
    String responseBody = buildBody(config);
    wireMock.stubFor(
        post(urlPathMatching(config.endpoint()))
            .willReturn(
                aResponse()
                    .withStatus(config.statusCode())
                    .withHeader("Content-Type", "application/json")
                    .withBody(responseBody)));
  }

  private String buildBody(FraudCheckMock config) {
    // Create the response body based on annotation parameters
    return String.format(
        """
                        {
                          "status": "%s",
                          "decision": "%s",
                          "riskScore": "%s",
                          "reason": "%s",
                          "requiresManualReview": %s,
                          "additionalVerificationRequired": %s
                        }""",
        config.status(),
        config.decision(),
        config.riskScore(),
        config.reason(),
        config.requiresManualReview(),
        config.additionalVerificationRequired());
  }

  @Override
  public void afterAll(ExtensionContext context) {
    // Останавливаем сервер после всех тестов (опционально)
    if (wireMockServer != null && wireMockServer.isRunning()) {
      wireMockServer.stop();
    }
  }

  public String getBaseUrl() {
    if (wireMockServer != null) {
      return "http://host.docker.internal:" + wireMockServer.port();
    }
    return null;
  }
}
