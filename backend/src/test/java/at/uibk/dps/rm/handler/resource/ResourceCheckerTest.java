package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UsedByOtherEntityException;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceCheckerTest {

    ResourceChecker resourceChecker;

    @Mock
    ResourceService resourceService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceChecker = new ResourceChecker(resourceService);
    }

    @Test
    void checkFindAllUnreservedFound(VertxTestContext testContext) {
        Resource resource1 = TestObjectProvider.createResource(1L);
        Resource resource2 = TestObjectProvider.createResource(2L);
        Resource resource3 = TestObjectProvider.createResource(3L);
        JsonArray resourcesJson = new JsonArray(List.of(JsonObject.mapFrom(resource1), JsonObject.mapFrom(resource2),
            JsonObject.mapFrom(resource3)));

        when(resourceService.findAllUnreserved()).thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllUnreserved()
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("resource_id")).isEqualTo(3L);
                    verify(resourceService).findAllUnreserved();
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllUnreservedEmpty(VertxTestContext testContext) {
        JsonArray resourcesJson = new JsonArray(List.of());

        when(resourceService.findAllUnreserved()).thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllUnreserved()
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    verify(resourceService).findAllUnreserved();
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllUnreservedNotFound(VertxTestContext testContext) {
        Single<JsonArray> handler = new SingleHelper<JsonArray>().getEmptySingle();

        when(resourceService.findAllUnreserved()).thenReturn(handler);

        resourceChecker.checkFindAllUnreserved()
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    // TODO: refine
    @Test
    void checkFindAllBySLOs(VertxTestContext testContext) {
        Function function = TestObjectProvider.createFunction(1L, "foo", "true");
        Resource resource1 = TestObjectProvider.createResource(1L);
        Resource resource2 = TestObjectProvider.createResource(2L);
        Resource resource3 = TestObjectProvider.createResource(3L);
        JsonArray resourcesJson = new JsonArray(List.of(JsonObject.mapFrom(resource1), JsonObject.mapFrom(resource2),
            JsonObject.mapFrom(resource3)));
        List<String> metrics = List.of("availability", "bandwidth");
        List<String> regions = new ArrayList<>();
        List<Long> resourceProviders = new ArrayList<>();
        List<Long> resourceTypes = new ArrayList<>();

        when(resourceService.findAllBySLOs(function.getFunctionId(), metrics, regions, resourceProviders, resourceTypes))
            .thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllBySLOs(function.getFunctionId(), metrics, regions, resourceProviders, resourceTypes)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(2).getLong("resource_id")).isEqualTo(3L);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }

    @Test
    void checkFindAllBySLOsEmpty(VertxTestContext testContext) {
        Function function = TestObjectProvider.createFunction(1L, "foo", "true");
        JsonArray resourcesJson = new JsonArray(List.of());
        List<String> metrics = List.of("availability", "bandwidth");
        List<String> regions = new ArrayList<>();
        List<Long> resourceProviders = new ArrayList<>();
        List<Long> resourceTypes = new ArrayList<>();

        when(resourceService.findAllBySLOs(function.getFunctionId(), metrics, regions, resourceProviders, resourceTypes))
            .thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllBySLOs(function.getFunctionId(), metrics, regions, resourceProviders, resourceTypes)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllBySLOsNotFound(VertxTestContext testContext) {
        Function function = TestObjectProvider.createFunction(1L, "foo", "true");
        List<String> metrics = List.of("availability", "bandwidth");
        List<String> regions = new ArrayList<>();
        List<Long> resourceProviders = new ArrayList<>();
        List<Long> resourceTypes = new ArrayList<>();
        Single<JsonArray> handler = new SingleHelper<JsonArray>().getEmptySingle();

        when(resourceService.findAllBySLOs(function.getFunctionId(), metrics, regions, resourceProviders, resourceTypes))
            .thenReturn(handler);

        resourceChecker.checkFindAllBySLOs(function.getFunctionId(), metrics, regions, resourceProviders, resourceTypes)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkFindAllByFunctionIdFound(VertxTestContext testContext) {
        long functionId = 1L;
        Resource resource1 = TestObjectProvider.createResource(1L);
        Resource resource2 = TestObjectProvider.createResource(2L);
        Resource resource3 = TestObjectProvider.createResource(3L);
        JsonArray resourcesJson = new JsonArray(List.of(JsonObject.mapFrom(resource1), JsonObject.mapFrom(resource2),
            JsonObject.mapFrom(resource3)));

        when(resourceService.findAllByFunctionId(functionId)).thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllByFunction(functionId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("resource_id")).isEqualTo(3L);
                    verify(resourceService).findAllByFunctionId(functionId);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByReservationEmpty(VertxTestContext testContext) {
        long functionId = 1L;
        JsonArray resourcesJson = new JsonArray(List.of());

        when(resourceService.findAllByFunctionId(functionId)).thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllByFunction(functionId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    verify(resourceService).findAllByFunctionId(functionId);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByMultipleReservationNotFound(VertxTestContext testContext) {
        long functionId = 1L;
        Single<JsonArray> handler = new SingleHelper<JsonArray>().getEmptySingle();

        when(resourceService.findAllByFunctionId(functionId)).thenReturn(handler);

        resourceChecker.checkFindAllByFunction(functionId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkOneUsedByResourceTypeFalse(VertxTestContext testContext) {
        long resourceTyeId = 1L;

        when(resourceService.existsOneByResourceType(resourceTyeId)).thenReturn(Single.just(false));

        resourceChecker.checkOneUsedByResourceType(resourceTyeId)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        verify(resourceService).existsOneByResourceType(resourceTyeId);
        testContext.completeNow();
    }

    @Test
    void checkOneUsedByResourceTypeTrue(VertxTestContext testContext) {
        long resourceTyeId = 1L;

        when(resourceService.existsOneByResourceType(resourceTyeId)).thenReturn(Single.just(true));

        resourceChecker.checkOneUsedByResourceType(resourceTyeId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UsedByOtherEntityException.class);
                    testContext.completeNow();
                })
            );
    }
}
