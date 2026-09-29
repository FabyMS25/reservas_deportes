package com.gamq.deportes.context;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.http.client.*;
import org.springframework.mock.http.client.*;
import org.springframework.web.client.*;
import org.springframework.test.web.client.MockRestServiceServer;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ExternalHttpLoggingInterceptorTest {
    private final ExternalHttpLoggingInterceptor interceptor =
            new ExternalHttpLoggingInterceptor("http://users.test/api", "http://ruat.test/api/ruat");
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();
    private final Logger logger = (Logger) LoggerFactory.getLogger("external.http");
    private Level previousLevel;

    @BeforeEach void capture() {
        appender.start();
        previousLevel = logger.getLevel();
        logger.setLevel(Level.INFO);
        logger.addAppender(appender);
    }
    @AfterEach void stop() {
        logger.detachAppender(appender);
        logger.setLevel(previousLevel);
        appender.stop();
    }
    private String logs() {
        return appender.list.stream().map(ILoggingEvent::getFormattedMessage)
                .reduce("", (a, b) -> a + "\n" + b);
    }
    private byte[] bytes(String s) { return s.getBytes(StandardCharsets.UTF_8); }

    @ParameterizedTest @CsvSource({
        "http://users.test/api/signin,usuarios", "http://ruat.test/api/ruat/payments,ruat",
        "https://other.test/service,external", "http://users.test/api-other,external"
    })
    void logsEveryDestinationWithoutChangingTheExchange(String url, String destination) throws Exception {
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.POST, URI.create(url));
        request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        request.getHeaders().setBearerAuth("header-secret");
        byte[] sent = bytes("{\"username\":\"private-user\",\"password\":\"private-password\",\"codigoModulo\":\"DEP\"}");
        byte[] received = bytes("{\"token\":\"opaque-private-token\",\"permissions\":[\"LEER\",\"ACTUALIZAR\"],\"nested\":{\"secret\":\"nested-secret\"}}");
        ClientHttpResponse result = interceptor.intercept(request, sent, (req, body) -> {
            assertArrayEquals(sent, body);
            assertEquals("Bearer header-secret", req.getHeaders().getFirst("Authorization"));
            assertNotNull(req.getHeaders().getFirst("X-Request-ID"));
            return new MockClientHttpResponse(received, HttpStatus.OK);
        });
        assertArrayEquals(received, result.getBody().readAllBytes());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        result.close();
        String logged = logs();
        assertTrue(logged.contains("hop=deportes-be->" + destination));
        assertTrue(logged.contains("direction=request"));
        assertTrue(logged.contains("direction=response-body"));
        assertTrue(logged.contains("status=200"));
        assertTrue(logged.contains("elapsedMs="));
        assertTrue(logged.contains("ACTUALIZAR"));
        for (String secret : List.of("header-secret", "private-user", "private-password", "opaque-private-token", "nested-secret"))
            assertFalse(logged.contains(secret), secret);
        assertEquals(1, appender.list.stream().filter(e -> e.getFormattedMessage().contains("[end response]")).count());
    }

    @Test void restTemplateErrorsKeepTheirOriginalStatusAndBody() {
        RestTemplate client = new RestTemplate();
        client.getInterceptors().add(interceptor);
        MockRestServiceServer server = MockRestServiceServer.bindTo(client).build();
        String response = "{\"mensaje\":\"Credenciales inválidas\",\"token\":\"private-response-token\"}";
        server.expect(requestTo("http://users.test/api/signin"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON).body(response));
        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class,
                () -> client.postForObject("http://users.test/api/signin", Map.of("password", "private-request-password"), String.class));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals(response, exception.getResponseBodyAsString());
        assertTrue(logs().contains("status=401"));
        assertFalse(logs().contains("private-response-token"));
        assertFalse(logs().contains("private-request-password"));
        server.verify();
    }

    @ParameterizedTest @CsvSource({
        "INFO,200,3", "WARN,200,0", "WARN,401,1",
        "ERROR,401,0", "ERROR,500,1", "OFF,500,0"
    })
    void sharedLogLevelControlsEveryDestination(String level, int status, int expectedEvents) throws Exception {
        logger.setLevel(Level.toLevel(level));
        for (String url : List.of("http://users.test/api/signin", "http://ruat.test/api/ruat/payments",
                "https://other.test/service")) {
            appender.list.clear();
            byte[] responseBody = bytes("{\"message\":\"test response\"}");
            try (ClientHttpResponse response = interceptor.intercept(
                    new MockClientHttpRequest(HttpMethod.GET, URI.create(url)), new byte[0],
                    (request, body) -> new MockClientHttpResponse(responseBody, HttpStatus.valueOf(status)))) {
                assertEquals(status, response.getRawStatusCode());
                assertArrayEquals(responseBody, response.getBody().readAllBytes());
            }
            assertEquals(expectedEvents, appender.list.size(), level + " " + status + " " + url);
            if (expectedEvents == 1) {
                assertTrue(logs().contains("status=" + status));
                assertFalse(logs().contains("test response"));
            }
        }
    }

    @Test void transportFailurePropagatesWithoutLoggingItsSecretMessage() {
        IOException failure = new IOException("http://user:private-password@host");
        assertSame(failure, assertThrows(IOException.class, () -> interceptor.intercept(
                new MockClientHttpRequest(HttpMethod.GET, URI.create("http://users.test/api/permissions")),
                new byte[0], (req, body) -> { throw failure; })));
        assertTrue(logs().contains("direction=error"));
        assertTrue(logs().contains("failure=IOException"));
        assertFalse(logs().contains("private-password"));
    }

    @Test void queryCredentialsAndNonJsonBodiesAreNotExposed() {
        String url = ExternalHttpLoggingInterceptor.safeUrl(URI.create(
                "http://user:password@users.test/api?token=opaque-secret&codigoModulo=DEP#secret"));
        assertFalse(url.contains("opaque-secret"));
        assertFalse(url.contains("password"));
        assertTrue(url.contains("codigoModulo=DEP"));
        assertFalse(ExternalHttpLoggingInterceptor.safeBody(bytes("plain-secret"), false).contains("plain-secret"));
        assertFalse(ExternalHttpLoggingInterceptor.safeBody(bytes("{\"token\":\"truncated-secret"), false).contains("truncated-secret"));
        assertTrue(ExternalHttpLoggingInterceptor.safeBody(new byte[65537], false).contains("omitido"));
    }

    @Test void applicationClientRegistersTheInterceptor() {
        RestTemplate client = new AppContext().restTemplate(new RestTemplateBuilder(),
                Duration.ofSeconds(1), Duration.ofSeconds(1), "http://users.test/api", "http://ruat.test/api/ruat");
        assertTrue(client.getInterceptors().stream().anyMatch(ExternalHttpLoggingInterceptor.class::isInstance));
    }
}
