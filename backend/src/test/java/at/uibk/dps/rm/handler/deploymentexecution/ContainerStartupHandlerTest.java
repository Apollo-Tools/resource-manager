package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.dto.function.InvocationMonitoringDTO;
import at.uibk.dps.rm.entity.dto.function.InvocationResponseDTO;
import at.uibk.dps.rm.entity.dto.function.InvokeFunctionDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.rxjava3.database.deployment.FunctionDeploymentService;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ServiceDeploymentService;
import at.uibk.dps.rm.service.rxjava3.monitoring.function.FunctionExecutionService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.misc.MultiMapUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.buffer.Buffer;
import io.vertx.rxjava3.core.http.HttpServerResponse;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link ContainerStartupHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ContainerStartupHandlerTest {

    private ContainerStartupHandler handler;

    @Mock
    private DeploymentExecutionChecker deploymentChecker;

    @Mock
    private ServiceProxyProvider serviceProxyProvider;

    @Mock
    private ServiceDeploymentService serviceDeploymentService;

    @Mock
    private FunctionDeploymentService functionDeploymentService;

    @Mock
    private FunctionExecutionService functionExecutionService;

    @Mock
    private RoutingContext rc;

    @Mock
    private HttpServerResponse response;

    private Account account;
    private ServiceDeployment sd;
    private FunctionDeployment fd;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new ContainerStartupHandler(deploymentChecker, serviceProxyProvider);
        lenient().when(serviceProxyProvider.getServiceDeploymentService()).thenReturn(serviceDeploymentService);
        lenient().when(serviceProxyProvider.getFunctionDeploymentService()).thenReturn(functionDeploymentService);
        lenient().when(serviceProxyProvider.getFunctionExecutionService()).thenReturn(functionExecutionService);
        account = TestAccountProvider.createAccount(1L);
        Deployment d1 = TestDeploymentProvider.createDeployment(1L);
        Resource r1 = TestResourceProvider.createResource(3L);
        sd = TestServiceProvider.createServiceDeployment(2L, r1, d1);
        fd = TestFunctionProvider.createFunctionDeployment(2L, r1, d1, "https://localhost:80/foo1");
    }

    public static Stream<Arguments> provideProcessDeployTerminateRequest() {
        Deployment d1 = TestDeploymentProvider.createDeployment(1L);
        ServiceDeployment sd = TestServiceProvider.createServiceDeployment(2L, 3L, d1);

        return Stream.of(
            Arguments.of(true, sd, true),
            Arguments.of(true, sd, false),
            Arguments.of(false, sd, true),
            Arguments.of(false, sd, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideProcessDeployTerminateRequest")
    void deployContainer(boolean isStartup, ServiceDeployment serviceDeployment, boolean successDeploymentTermination,
            VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(serviceDeployment.getResourceDeploymentId()));
        when(serviceDeploymentService.findOneForDeploymentAndTermination(serviceDeployment.getResourceDeploymentId(),
            account.getAccountId())).thenReturn(Single.just(JsonObject.mapFrom(serviceDeployment)));
        if (!isStartup && successDeploymentTermination) {
            when(rc.response()).thenReturn(response);
            when(response.setStatusCode(204)).thenReturn(response);
            when(response.end()).thenReturn(Completable.complete());
        }
        if (isStartup && successDeploymentTermination) {
            when(rc.response()).thenReturn(response);
            when(response.setStatusCode(200)).thenReturn(response);
            when(response.end(argThat((String response) -> {
                JsonObject body = new JsonObject(response);
                return body.containsKey("startup_time_seconds") && body.getDouble("startup_time_seconds") > 0.0;
            }))).thenReturn(Completable.complete());
        }
        Completable completable;
        if (isStartup) {
            when(deploymentChecker.startContainer(serviceDeployment))
                .thenReturn(successDeploymentTermination ? Single.just(new JsonObject()) :
                    Single.error(DeploymentTerminationFailedException::new));
            completable = handler.deployContainer(rc);
        } else {
            when(deploymentChecker.stopContainer(serviceDeployment))
                .thenReturn(successDeploymentTermination ? Completable.complete() :
                    Completable.error(DeploymentTerminationFailedException::new));
            completable = handler.terminateContainer(rc);
        }

        completable.subscribe(() -> testContext.verify(() -> {
                if (!successDeploymentTermination) {
                    testContext.failNow("methods did not throw exception");
                }
                testContext.completeNow();
            }),
                throwable -> testContext.verify(() -> {
                    if (successDeploymentTermination) {
                        testContext.failNow("methods has thrown exception");
                    }
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage())
                        .isEqualTo("Deployment failed. See deployment logs for details.");
                    testContext.completeNow();
                }));
    }

    @Test
    void deployContainerNotFound(VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(sd.getResourceDeploymentId()));
        when(serviceDeploymentService.findOneForDeploymentAndTermination(sd.getResourceDeploymentId(),
            account.getAccountId())).thenReturn(Single.error(NotFoundException::new));

        handler.deployContainer(rc)
            .subscribe(() -> testContext.failNow("methods did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    assertThat(throwable.getMessage())
                        .isEqualTo("not found");
                    testContext.completeNow();
                })
            );
    }

    public static Stream<Arguments> provideInvokeFunction() {
        JsonMapperConfig.configJsonMapper();
        String resultBody = "{\"result\": 42}";
        InvocationMonitoringDTO monitoringData = TestDTOProvider
            .createInvocationMonitoringDTO(235, 1699023299133L);
        InvocationResponseDTO invocationResponseDTO = TestDTOProvider
            .createInvocationResponseDTO(monitoringData, resultBody);

        return Stream.of(
            Arguments.of("{\"arg\":1}", "invocationresult", "invocationresult"),
            Arguments.of("nullBody", "invocationresult", "invocationresult"),
            Arguments.of("nullBuffer", "invocationresult", "invocationresult"),
            Arguments.of("{\"arg\":1}", JsonObject.mapFrom(invocationResponseDTO).encode(), resultBody)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvokeFunction")
    void invokeFunction(String body, String invocationResult, String responseBody, VertxTestContext testContext) {
        MultiMap initialHeaders = MultiMap.caseInsensitiveMultiMap();
        initialHeaders.add("Content-Type", "application/json")
            .add("Authorization", "Bearer jiogghawgewgwa==")
            .add("Host", "localhost")
            .add("User-Agent", "Mozilla/5.0");
        MultiMap proxyHeaders = MultiMap.caseInsensitiveMultiMap();
        proxyHeaders.add("Content-Type", "application/json")
            .add("apollo-request-type", "rm");
        Map<String, JsonArray> serializedHeaders = MultiMapUtility.serializeMultimap(proxyHeaders);

        Buffer buffer = Buffer.buffer();
        if (body.equals("nullBody")) {
            RoutingContextMockHelper.mockNullBody(rc);
        } else if (body.equals("nullBuffer")) {
            RoutingContextMockHelper.mockBody(rc, (Buffer) null);
        } else {
            buffer = Buffer.buffer(body);
            RoutingContextMockHelper.mockBody(rc, buffer);
        }
        InvokeFunctionDTO invokeFunctionDTO = TestDTOProvider.createInvokeFunctionDTO(invocationResult, 200);

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockHeaders(rc, initialHeaders);
        when(rc.pathParam("id")).thenReturn(String.valueOf(fd.getResourceDeploymentId()));
        when(functionDeploymentService.findOneForInvocation(fd.getResourceDeploymentId(), account.getAccountId()))
            .thenReturn(Single.just(JsonObject.mapFrom(fd)));
        when(functionExecutionService.invokeFunction(fd.getDirectTriggerUrl(), buffer.toString(), serializedHeaders))
            .thenReturn(Single.just(JsonObject.mapFrom(invokeFunctionDTO)));
        when(rc.response()).thenReturn(response);
        when(response.setStatusCode(200)).thenReturn(response);
        doReturn(Completable.complete()).when(response).end(argThat((String result) -> result.equals(responseBody)));
        if (!body.equals("nullBody") && !body.equals("nullBuffer") && !invocationResult.equals("invocationresult")) {
            when(functionDeploymentService.saveExecTime(2L, 235, "{\"arg\":1}"))
                .thenReturn(Completable.complete());
        }

        handler.invokeFunction(rc)
            .subscribe(testContext::completeNow, throwable -> testContext.failNow("methods has thrown exception"));
    }
}
