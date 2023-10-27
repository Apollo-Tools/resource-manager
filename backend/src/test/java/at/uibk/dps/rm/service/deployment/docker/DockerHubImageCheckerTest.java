package at.uibk.dps.rm.service.deployment.docker;

import at.uibk.dps.rm.entity.dto.credentials.DockerCredentials;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.mockprovider.Mockprovider;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.client.HttpResponse;
import io.vertx.rxjava3.ext.web.client.WebClient;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.ext.web.client.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link DockerHubImageChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DockerHubImageCheckerTest {

    @Mock
    private HttpRequest<Buffer> httpRequest;

    @Mock
    private HttpResponse<Buffer> httpResponse;

    private DockerCredentials dockerCredentials;
    private Function f1, f2, f3;
    private FunctionDeployment fd1, fd2, fd3, fd4;


    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        dockerCredentials = TestDTOProvider.createDockerCredentials();
        f1 = spy(TestFunctionProvider.createFunction(1L, "func1", "code"));
        f2 = spy(TestFunctionProvider.createFunction(2L, "func2", "code"));
        f3 = spy(TestFunctionProvider.createFunction(3L, "func3", "code"));
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        fd1 = TestFunctionProvider.createFunctionDeployment(1L, f1, r1, new Deployment());
        fd2 = TestFunctionProvider.createFunctionDeployment(2L, f2, r1, new Deployment());
        fd3 = TestFunctionProvider.createFunctionDeployment(3L, f3, r1, new Deployment());
        fd4 = TestFunctionProvider.createFunctionDeployment(4L, f1, r2, new Deployment());
    }

    @Test
    void getNecessaryFunctionBuilds(Vertx vertx, VertxTestContext testContext) {
        DockerHubImageChecker checker = new DockerHubImageChecker(vertx, dockerCredentials);
        // 2023-07-22 04:26:40 UTC
        when(f1.getUpdatedAt()).thenReturn(new Timestamp(1690000000000L));
        when(f2.getUpdatedAt()).thenReturn(new Timestamp(1690000000000L));
        when(f3.getUpdatedAt()).thenReturn(new Timestamp(1690000000000L));
        when(httpRequest.send()).thenReturn(Single.just(httpResponse));
        when(httpResponse.statusCode()).thenReturn(404).thenReturn(200);
        when(httpResponse.bodyAsJsonObject())
            .thenReturn(new JsonObject("{\"tag_last_pushed\": \"2023-08-22T01:00:00.000Z\"}"))
            .thenReturn(new JsonObject("{\"tag_last_pushed\": \"2023-06-22T01:00:00.000Z\"}"));

        try(MockedConstruction<WebClient> ignore = Mockprovider
                .mockWebClientDockerHubCheck(List.of("testuser/func1_python39", "testuser/func2_python39", "testuser/func3_python39"),
                    List.of("latest", "latest", "latest"),
                    httpRequest)) {
            checker.getNecessaryFunctionBuilds(List.of(fd1, fd2, fd3, fd4))
                .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.contains(f1)).isEqualTo(true);
                    assertThat(result.contains(f3)).isEqualTo(true);
                    testContext.completeNow();
                }), throwable -> testContext.failNow("method has thrown exception"));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "local-reg"})
    void getNecessaryFunctionBuildsInvalidCreds(String type, Vertx vertx, VertxTestContext testContext) {
        dockerCredentials.setRegistry("local-reg.io");
        DockerHubImageChecker checker = new DockerHubImageChecker(vertx,
            type.equals("null") ? null : dockerCredentials);

        checker.getNecessaryFunctionBuilds(List.of(fd1, fd2, fd3, fd4))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.contains(f1)).isEqualTo(true);
                assertThat(result.contains(f2)).isEqualTo(true);
                assertThat(result.contains(f3)).isEqualTo(true);
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void isUpToDateValidationFailed(Vertx vertx, VertxTestContext testContext) {
        DockerHubImageChecker checker = new DockerHubImageChecker(vertx, dockerCredentials);
        when(httpRequest.send()).thenReturn(Single.just(httpResponse));
        when(httpResponse.statusCode()).thenReturn(503);

        try(MockedConstruction<WebClient> ignore = Mockprovider
            .mockWebClientDockerHubCheck(List.of("func1"), List.of("latest"), httpRequest)) {
            checker.isUpToDate("func1", "latest", new Timestamp(1690000000000L))
                .subscribe(result ->testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                   assertThat(throwable).isInstanceOf(RuntimeException.class);
                   assertThat(throwable.getMessage()).isEqualTo("docker image validation failed");
                   testContext.completeNow();
                }));
        }
    }
}
