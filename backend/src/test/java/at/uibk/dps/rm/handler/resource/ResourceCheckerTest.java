package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.exception.UsedByOtherEntityException;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ResourceChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceCheckerTest {

    private ResourceChecker resourceChecker;

    @Mock
    private ResourceService resourceService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceChecker = new ResourceChecker(resourceService);
    }

    @Test
    void checkFindAllBySLOs(VertxTestContext testContext) {
        JsonArray resourcesJson = TestResourceProvider.createGetAllResourcesArray();
        List<String> metrics = List.of("availability", "bandwidth");
        List<Long> regions = new ArrayList<>();
        List<Long> resourceProviders = new ArrayList<>();
        List<Long> resourceTypes = new ArrayList<>();

        when(resourceService.findAllBySLOs(metrics, regions, resourceProviders, resourceTypes))
            .thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllBySLOs(metrics, regions, resourceProviders, resourceTypes)
            .subscribe(result -> testContext.verify(() -> {
                        verifyGetAllResourceRequest(result);
                testContext.completeNow();
            }),
            throwable -> testContext.verify(() -> fail("method has thrown exception"))
        );
    }

    @Test
    void checkFindAllBySLOsEmpty(VertxTestContext testContext) {
        JsonArray resourcesJson = new JsonArray(List.of());
        List<String> metrics = List.of("availability", "bandwidth");
        List<Long> regions = new ArrayList<>();
        List<Long> resourceProviders = new ArrayList<>();
        List<Long> resourceTypes = new ArrayList<>();

        when(resourceService.findAllBySLOs(metrics, regions, resourceProviders, resourceTypes))
            .thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllBySLOs(metrics, regions, resourceProviders, resourceTypes)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByFunction(VertxTestContext testContext) {
        long functionId = 1L;
        JsonArray resourcesJson = TestResourceProvider.createGetAllResourcesArray();

        when(resourceService.findAllByFunctionId(functionId)).thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllByFunction(functionId)
            .subscribe(result -> testContext.verify(() -> {
                        verifyGetAllResourceRequest(result);
                    verify(resourceService).findAllByFunctionId(functionId);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByFunctionEmpty(VertxTestContext testContext) {
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
    void checkFindAllByEnsembleFound(VertxTestContext testContext) {
        long ensembleId = 1L;
        JsonArray resourcesJson = TestResourceProvider.createGetAllResourcesArray();

        when(resourceService.findAllByEnsembleId(ensembleId)).thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllByEnsemble(ensembleId)
                .subscribe(result -> testContext.verify(() -> {
                            verifyGetAllResourceRequest(result);
                            testContext.completeNow();
                        }),
                        throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
    }

    @Test
    void checkFindAllByEnsembleEmpty(VertxTestContext testContext) {
        long ensembleId = 1L;
        JsonArray resourcesJson = new JsonArray(List.of());

        when(resourceService.findAllByEnsembleId(ensembleId)).thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllByEnsemble(ensembleId)
                .subscribe(result -> testContext.verify(() -> {
                            assertThat(result.size()).isEqualTo(0);
                            testContext.completeNow();
                        }),
                        throwable -> testContext.verify(() -> fail("method has thrown exception"))
                );
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void checkOneUsedByResourceType(boolean isUsed, VertxTestContext testContext) {
        long resourceTyeId = 1L;

        when(resourceService.existsOneByResourceType(resourceTyeId)).thenReturn(Single.just(isUsed));

        resourceChecker.checkOneUsedByResourceType(resourceTyeId)
            .blockingSubscribe(() -> {
                    if (isUsed) {
                        testContext.verify(() -> fail("method did not throw exception"));
                    }
                },
                throwable -> {
                    if (isUsed) {
                        assertThat(throwable).isInstanceOf(UsedByOtherEntityException.class);
                        testContext.completeNow();
                    } else {
                        testContext.verify(() -> fail("method has thrown exception"));
                    }
                }
            );
        testContext.completeNow();
    }

    private void verifyGetAllResourceRequest(JsonArray result) {
        assertThat(result.size()).isEqualTo(3);
        for (int i = 0; i < 3; i++) {
            assertThat(result.getJsonObject(i).getLong("resource_id")).isEqualTo(i+1);
        }
    }
}
