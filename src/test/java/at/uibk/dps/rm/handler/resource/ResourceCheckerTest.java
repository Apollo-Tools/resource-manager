package at.uibk.dps.rm.handler.resource;

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
    void checkFindAllByMultipleMetricsFound(VertxTestContext testContext) {
        Resource resource1 = TestObjectProvider.createResource(1L);
        Resource resource2 = TestObjectProvider.createResource(2L);
        Resource resource3 = TestObjectProvider.createResource(3L);
        JsonArray resourcesJson = new JsonArray(List.of(JsonObject.mapFrom(resource1), JsonObject.mapFrom(resource2),
            JsonObject.mapFrom(resource3)));
        List<String> metrics = List.of("availability", "bandwidth");

        when(resourceService.findAllByMultipleMetrics(metrics)).thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllByMultipleMetrics(metrics)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(2).getLong("resource_id")).isEqualTo(3L);
                verify(resourceService).findAllByMultipleMetrics(metrics);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
        );
    }

    @Test
    void checkFindAllByMultipleMetricsNotFound(VertxTestContext testContext) {
        List<String> metrics = List.of("availability", "bandwidth");
        Single<JsonArray> handler = new SingleHelper<JsonArray>().getEmptySingle();

        when(resourceService.findAllByMultipleMetrics(metrics)).thenReturn(handler);

        resourceChecker.checkFindAllByMultipleMetrics(metrics)
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
                throwable -> testContext.verify(() -> fail("method did throw exception"))
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

    @Test
    void checkExistsOneAndIsNotReservedTrue(VertxTestContext testContext) {
        long resourceId = 1L;

        when(resourceService.existsOneAndNotReserved(resourceId)).thenReturn(Single.just(true));

        resourceChecker.checkExistsOneAndIsNotReserved(resourceId)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(resourceService).existsOneAndNotReserved(resourceId);
        testContext.completeNow();
    }

    @Test
    void checkExistsOneAndIsNotReservedFalse(VertxTestContext testContext) {
        long resourceId = 1L;

        when(resourceService.existsOneAndNotReserved(resourceId)).thenReturn(Single.just(false));

        resourceChecker.checkExistsOneAndIsNotReserved(resourceId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );

        verify(resourceService).existsOneAndNotReserved(resourceId);
        testContext.completeNow();
    }
}
