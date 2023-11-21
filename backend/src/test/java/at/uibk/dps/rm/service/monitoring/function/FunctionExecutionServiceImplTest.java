package at.uibk.dps.rm.service.monitoring.function;

import at.uibk.dps.rm.service.database.deployment.DeploymentServiceImpl;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import io.vertx.rxjava3.ext.web.client.HttpResponse;
import io.vertx.rxjava3.ext.web.client.WebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link DeploymentServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionExecutionServiceImplTest {

    private FunctionExecutionService executionService;

    @Mock
    private WebClient webClient;

    @Mock
    private HttpRequest<Buffer> proxyRequest;

    @Mock
    private HttpResponse<Buffer> proxyResponse;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        executionService = new FunctionExecutionServiceImpl(webClient);
    }

    @Test
    void invokeFunction(VertxTestContext testContext) {
        String triggerUrl = "http://localhost:8080";
        String requestBody = "{\"input\": 3}";
        Map<String, JsonArray> headers = new HashMap<>();
        headers.put("header", new JsonArray(List.of("\"value\"")));
        String invocationResult = "\"result\"";

        when(webClient.postAbs(triggerUrl)).thenReturn(proxyRequest);
        doReturn(proxyRequest)
            .when(proxyRequest)
            .putHeaders(argThat(multiMap -> multiMap.size() == 1 && multiMap.get("header").equals("value")));
        when(proxyRequest.sendBuffer(Buffer.buffer(requestBody))).thenReturn(Single.just(proxyResponse));
        when(proxyResponse.bodyAsString()).thenReturn(invocationResult);
        when(proxyResponse.statusCode()).thenReturn(200);

        executionService.invokeFunction(triggerUrl, requestBody, headers,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getString("body")).isEqualTo("\"result\"");
                assertThat(result.getInteger("status_code")).isEqualTo(200);
                testContext.completeNow();
            })));
    }
}
