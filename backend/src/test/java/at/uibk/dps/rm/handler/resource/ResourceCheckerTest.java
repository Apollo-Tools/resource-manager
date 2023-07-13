package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.entity.dto.deployment.ServiceResourceIds;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
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
        List<Long> regions = List.of();
        List<Long> resourceProviders = List.of();
        List<Long> resourceTypes = List.of();
        List<Long> platforms = List.of();
        List<Long> environments = List.of();
        JsonObject data = new JsonObject();

        when(resourceService.findAllBySLOs(data))
            .thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllBySLOs(data)
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
        List<Long> regions = List.of();
        List<Long> resourceProviders = List.of();
        List<Long> resourceTypes = List.of();
        List<Long> platforms = List.of();
        List<Long> environments = List.of();
        JsonObject data = new JsonObject();

        when(resourceService.findAllBySLOs(data))
            .thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllBySLOs(data)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
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

    @Test
    void checkFindAllByResourceIds(VertxTestContext testContext) {
        List<Long> resourceIds = List.of(1L, 2L, 3L);
        JsonArray resourcesJson = TestResourceProvider.createGetAllResourcesArray();

        when(resourceService.findAllByResourceIds(resourceIds)).thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllByResourceIds(resourceIds)
            .subscribe(result -> testContext.verify(() -> {
                    verifyGetAllResourceRequest(result);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByResourceIdsEmpty(VertxTestContext testContext) {
        List<Long> resourceIds = List.of(1L, 2L, 3L);
        JsonArray resourcesJson = new JsonArray(List.of());

        when(resourceService.findAllByResourceIds(resourceIds)).thenReturn(Single.just(resourcesJson));

        resourceChecker.checkFindAllByResourceIds(resourceIds)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @ParameterizedTest
    @CsvSource({
        "true, true, true",
        "true, false, false",
        "false, true, false",
        "false, false, false"
    })
    void checkExistAllByIdsAndResourceType(boolean allFrExist, boolean allSrExist,
            boolean isValid, VertxTestContext testContext) {
        List<FunctionResourceIds> functionResourceMappings = TestFunctionProvider.createFunctionResourceIdsList(1L, 2L,
            3L);
        List<ServiceResourceIds> serviceResourceMappings = TestServiceProvider.createServiceResourceIdsList(1L);
        Set<Long> functionResourceIds =
            functionResourceMappings.stream().map(FunctionResourceIds::getResourceId).collect(Collectors.toSet());
        Set<Long> serviceResourceIds =
            serviceResourceMappings.stream().map(ServiceResourceIds::getResourceId).collect(Collectors.toSet());

        when(resourceService.existsAllByIdsAndResourceTypes(functionResourceIds,
            List.of(ResourceTypeEnum.FAAS.getValue()))).thenReturn(Single.just(allFrExist));
        when(resourceService.existsAllByIdsAndResourceTypes(serviceResourceIds,
            List.of(ResourceTypeEnum.CONTAINER.getValue()))).thenReturn(Single.just(allSrExist));

        resourceChecker.checkExistAllByIdsAndResourceType(serviceResourceMappings, functionResourceMappings)
            .blockingSubscribe(() -> testContext.verify(() -> {
                    if (!isValid) {
                        fail("method did not throw exception");
                    }
                    testContext.completeNow();
                }), throwable -> testContext.verify(() -> {
                    if (isValid) {
                        fail("method has thrown exception");
                    } else {
                        assertThat(throwable).isInstanceOf(NotFoundException.class);
                        testContext.completeNow();
                    }
                })
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
