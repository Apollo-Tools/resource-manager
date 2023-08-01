package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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


    private void verifyGetAllResourceRequest(JsonArray result) {
        assertThat(result.size()).isEqualTo(3);
        for (int i = 0; i < 3; i++) {
            assertThat(result.getJsonObject(i).getLong("resource_id")).isEqualTo(i+1);
        }
    }
}
