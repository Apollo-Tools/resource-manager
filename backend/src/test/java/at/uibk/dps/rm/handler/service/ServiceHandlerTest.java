package at.uibk.dps.rm.handler.service;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ServiceHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceHandlerTest {

    private ServiceHandler serviceHandler;

    @Mock
    private ServiceChecker serviceChecker;

    @Mock
    private ServiceTypeChecker serviceTypeChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        serviceHandler = new ServiceHandler(serviceChecker, serviceTypeChecker);
    }

    private static Stream<Arguments> provideRequestBody() {
        String name = "servicex-highmemory";
        long serviceTypeId = 1L;
        JsonObject serviceType1 = JsonObject.mapFrom(TestServiceProvider.createServiceType(serviceTypeId, "NoService"));
        JsonObject serviceType2 = JsonObject.mapFrom(TestServiceProvider.createServiceType(serviceTypeId, "NodePort"));
        String ports1 = "[]";
        String ports2 = "[\"80:8000\"]";
        JsonObject requestBody1 = new JsonObject("{\"name\": \"" + name + "\", \"image\": \"user/example\", " +
            "\"replicas\": 1, \"ports\":"+ ports1 +",\"cpu\": 0.1,\"memory\": 128,\"service_type\": {" +
            "\"service_type_id\": " + serviceTypeId + "}}");
        JsonObject requestBody2 = new JsonObject("{\"name\": \"" + name + "\", \"image\": \"user/example\", " +
            "\"replicas\": 1, \"ports\":"+ ports2 +",\"cpu\": 0.1,\"memory\": 128,\"service_type\": {" +
            "\"service_type_id\": " + serviceTypeId + "}}");
        return Stream.of(Arguments.of(serviceTypeId, requestBody1, serviceType1),
            Arguments.of(serviceTypeId, requestBody2, serviceType2));
    }

    @ParameterizedTest
    @MethodSource("provideRequestBody")
    void postOneValid(long serviceTypeId, JsonObject requestBody, JsonObject serviceType,
            VertxTestContext testContext) {
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(serviceTypeChecker.checkFindOne(serviceTypeId)).thenReturn(Single.just(serviceType));
        when(serviceChecker.checkForDuplicateEntity(requestBody)).thenReturn(Completable.complete());
        when(serviceChecker.submitCreate(requestBody)).thenReturn(Single.just(requestBody));

        serviceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject("service_type").getLong("service_type_id"))
                        .isEqualTo(1L);
                    assertThat(result.getString("name")).isEqualTo("servicex-highmemory");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @ParameterizedTest
    @MethodSource("provideRequestBody")
    void postOneRuntimeNotFound(long serviceTypeId, JsonObject requestBody, JsonObject ignoredServiceType,
            VertxTestContext testContext) {
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(serviceTypeChecker.checkFindOne(serviceTypeId)).thenReturn(Single.error(NotFoundException::new));
        when(serviceChecker.checkForDuplicateEntity(requestBody)).thenReturn(Completable.complete());

        serviceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method has thrown exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @MethodSource("provideRequestBody")
    void postOneAlreadyExists(long serviceTypeId, JsonObject requestBody, JsonObject serviceType,
            VertxTestContext testContext) {
        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(serviceTypeChecker.checkFindOne(serviceTypeId)).thenReturn(Single.just(serviceType));
        when(serviceChecker.checkForDuplicateEntity(requestBody))
            .thenReturn(Completable.error(AlreadyExistsException::new));

        serviceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @ParameterizedTest
    @CsvSource({
        "NoService, [\"80:8000\"]",
        "LoadBalancer, []"
    })
    void postOneInvalidServicePorts(String serviceTypeName, String ports, VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject("{\"ports\": " + ports + ",\"service_type\": {\"service_type_id\": " +
            "1}}");
        JsonObject serviceType = JsonObject.mapFrom(TestServiceProvider.createServiceType(1L, serviceTypeName));

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(serviceTypeChecker.checkFindOne(1L)).thenReturn(Single.just(serviceType));
        when(serviceChecker.checkForDuplicateEntity(requestBody)).thenReturn(Completable.complete());

        serviceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("invalid ports for service selection");
                    testContext.completeNow();
                })
            );
    }
}
