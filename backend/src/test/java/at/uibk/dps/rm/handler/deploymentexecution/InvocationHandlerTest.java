package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.dto.deployment.StartupShutdownServicesDTO;
import at.uibk.dps.rm.entity.dto.deployment.StartupShutdownServicesRequestDTO;
import at.uibk.dps.rm.entity.dto.function.InvocationMonitoringDTO;
import at.uibk.dps.rm.entity.dto.function.InvocationResponseBodyDTO;
import at.uibk.dps.rm.entity.dto.function.InvokeFunctionDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.ServiceStartupShutdownTime;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.DeploymentTerminationFailedException;
import at.uibk.dps.rm.exception.ForbiddenException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.rxjava3.database.deployment.DeploymentService;
import at.uibk.dps.rm.service.rxjava3.database.deployment.FunctionDeploymentService;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ServiceDeploymentService;
import at.uibk.dps.rm.service.rxjava3.monitoring.function.FunctionExecutionService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricpusher.ServiceStartupShutdownPushService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricpusher.FunctionInvocationPushService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.misc.MultiMapUtility;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link InvocationHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class InvocationHandlerTest {

    private InvocationHandler handler;

    @Mock
    private DeploymentExecutionChecker deploymentChecker;

    @Mock
    private ServiceProxyProvider serviceProxyProvider;

    @Mock
    private DeploymentService deploymentService;

    @Mock
    private ServiceDeploymentService serviceDeploymentService;

    @Mock
    private FunctionDeploymentService functionDeploymentService;

    @Mock
    private FunctionExecutionService functionExecutionService;

    @Mock
    private ServiceStartupShutdownPushService startupShutdownPushService;

    @Mock
    private FunctionInvocationPushService functionInvocationPushService;

    @Mock
    private RoutingContext rc;

    @Mock
    private HttpServerResponse response;

    private Account account;
    private Deployment d1;
    private ServiceDeployment sd1, sd2;
    private FunctionDeployment fd;
    private StartupShutdownServicesDTO startupShutdownServicesDTO;
    private StartupShutdownServicesRequestDTO requestIgnoreState, requestNotIgnoreState;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new InvocationHandler(deploymentChecker, serviceProxyProvider);
        lenient().when(serviceProxyProvider.getDeploymentService()).thenReturn(deploymentService);
        lenient().when(serviceProxyProvider.getServiceDeploymentService()).thenReturn(serviceDeploymentService);
        lenient().when(serviceProxyProvider.getFunctionDeploymentService()).thenReturn(functionDeploymentService);
        lenient().when(serviceProxyProvider.getFunctionExecutionService()).thenReturn(functionExecutionService);
        lenient().when(serviceProxyProvider.getFunctionInvocationPushService())
            .thenReturn(functionInvocationPushService);
        lenient().when(serviceProxyProvider.getServiceStartStopPushService())
            .thenReturn(startupShutdownPushService);
        account = TestAccountProvider.createAccount(1L);
        d1 = TestDeploymentProvider.createDeployment(1L);
        Resource r1 = TestResourceProvider.createResource(3L);
        sd1 = TestServiceProvider.createServiceDeployment(2L, r1, d1);
        sd2 = TestServiceProvider.createServiceDeployment(3L, r1, d1);
        startupShutdownServicesDTO = TestDTOProvider.createStartupShutdownServicesDTO(d1,
            List.of(sd1, sd2));
        requestIgnoreState = TestDTOProvider.createStartupShutdownServicesRequest(1L,
            List.of(2L, 3L), true);
        requestNotIgnoreState = TestDTOProvider.createStartupShutdownServicesRequest(1L,
            List.of(2L, 3L), false);
        fd = TestFunctionProvider.createFunctionDeployment(2L, r1, d1, "https://localhost:80/foo1");
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, admin",
        "true, false, admin",
        "false, true, default",
        "false, false, default"
    })
    void startupShutdownService(boolean isStartup, boolean successDeploymentTermination, String role,
            VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account, role);
        RoutingContextMockHelper.mockBody(rc, JsonObject.mapFrom(requestNotIgnoreState));
        when(deploymentService.findOneForServiceOperationByIdAndAccountId(d1.getDeploymentId(), account.getAccountId(),
            false)).thenReturn(Single.just(JsonObject.mapFrom(d1)));
        when(serviceDeploymentService.findAllForServiceOperation(List.of(2L, 3L), account.getAccountId(), d1.getDeploymentId()))
            .thenReturn(Single.just(new JsonArray(Json.encode(List.of(sd1, sd2)))));
        when(deploymentService.finishServiceOperation(d1.getDeploymentId(), account.getAccountId()))
            .thenReturn(Completable.complete());
        if (!isStartup && successDeploymentTermination) {
            when(rc.response()).thenReturn(response);
            when(response.setStatusCode(200)).thenReturn(response);
            when(response.end(argThat((String response) -> {
                JsonObject body = new JsonObject(response);
                return body.containsKey("shutdown_time_seconds") &&
                    body.getDouble("shutdown_time_seconds") > 0.0;
            }))).thenReturn(Completable.complete());
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
            when(deploymentChecker.startupServices(startupShutdownServicesDTO))
                .thenReturn(successDeploymentTermination ? Single.just(new JsonObject()) :
                    Single.error(DeploymentTerminationFailedException::new));
            completable = handler.startupServices(rc);
        } else {
            when(deploymentChecker.shutdownServices(startupShutdownServicesDTO))
                .thenReturn(successDeploymentTermination ? Completable.complete() :
                    Completable.error(DeploymentTerminationFailedException::new));
            completable = handler.shutdownServices(rc);
        }

        if (successDeploymentTermination) {
            when(startupShutdownPushService.composeAndPushMetric(argThat(metrics -> {
                ServiceStartupShutdownTime serviceStartupShutdownTime = metrics.mapTo(ServiceStartupShutdownTime.class);
                return !serviceStartupShutdownTime.getId().isBlank() && serviceStartupShutdownTime.getExecutionTime() > 0.0 &&
                    serviceStartupShutdownTime.getServiceDeployments().equals(List.of(sd1, sd2)) &&
                    serviceStartupShutdownTime.getIsStartup() == isStartup;
            })))
                .thenReturn(Completable.complete());
        }

        completable.subscribe(() -> testContext.verify(() -> {
                if (!successDeploymentTermination) {
                    testContext.failNow("method did not throw exception");
                }
                testContext.completeNow();
            }),
                throwable -> testContext.verify(() -> {
                    if (successDeploymentTermination) {
                        testContext.failNow("method has thrown exception");
                    }
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage())
                        .isEqualTo("Startup/Shutdown failed. See deployment logs for details.");
                    testContext.completeNow();
                }));
    }

    @Test
    void startupShutdownServicesServicesNotFound(VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, JsonObject.mapFrom(requestNotIgnoreState));

        when(deploymentService.findOneForServiceOperationByIdAndAccountId(d1.getDeploymentId(), account.getAccountId(),
            false)).thenReturn(Single.just(JsonObject.mapFrom(d1)));
        when(serviceDeploymentService.findAllForServiceOperation(List.of(2L, 3L), account.getAccountId(), d1.getDeploymentId()))
            .thenReturn(Single.just(new JsonArray(List.of())));
        when(deploymentService.finishServiceOperation(d1.getDeploymentId(), account.getAccountId()))
            .thenReturn(Completable.complete());
        when(rc.response()).thenReturn(response);
        when(response.setStatusCode(204)).thenReturn(response);
        when(response.end()).thenReturn(Completable.complete());

        handler.startupServices(rc)
            .subscribe(testContext::completeNow, throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void startupShutdownServicesDeploymentNotFound(VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, JsonObject.mapFrom(requestNotIgnoreState));

        when(deploymentService.findOneForServiceOperationByIdAndAccountId(d1.getDeploymentId(), account.getAccountId(),
            false)).thenReturn(Single.error(new NotFoundException(Deployment.class)));

        handler.startupServices(rc)
            .subscribe(() -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    assertThat(throwable.getMessage()).isEqualTo("Deployment not found");
                    testContext.completeNow();
                }));
    }

    @Test
    void startupShutdownServicesNotAllowed(VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        RoutingContextMockHelper.mockBody(rc, JsonObject.mapFrom(requestIgnoreState));

        handler.startupServices(rc)
            .subscribe(() -> testContext.failNow("method did not throw exception"),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(ForbiddenException.class);
                    assertThat(throwable.getMessage())
                        .isEqualTo("this operation is not allowed with the specified parameters");
                    testContext.completeNow();
                }));
    }

    public static Stream<Arguments> provideInvokeFunction() {
        JsonMapperConfig.configJsonMapper();
        String resultBody = "{\"result\": 42}";
        InvocationMonitoringDTO monitoringData = TestDTOProvider
            .createInvocationMonitoringDTO(235, 1699023299133L);
        InvocationResponseBodyDTO invocationResponseDTO = TestDTOProvider
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
            when(functionInvocationPushService.composeAndPushMetric(0.235d, 1L,
                2L, 22L, 3L, "{\"arg\":1}"))
            .thenReturn(Completable.complete());
        }

        handler.invokeFunction(rc)
            .subscribe(testContext::completeNow, throwable -> testContext.failNow("methods has thrown exception"));
    }
}
