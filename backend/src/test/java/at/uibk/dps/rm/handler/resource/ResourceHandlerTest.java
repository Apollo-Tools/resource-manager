package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.handler.resourceprovider.RegionChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestPlatformProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ResourceHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceHandlerTest {

    private ResourceHandler resourceHandler;

    @Mock
    private ResourceChecker resourceChecker;

    @Mock
    private RegionChecker regionChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceHandler = new ResourceHandler(resourceChecker, regionChecker);
    }

    private JsonObject composeResource() {
        Platform platform = TestPlatformProvider.createPlatformFaas(11L, "lambda");
        Region region = TestResourceProviderProvider.createRegion(22L, "us-east-1");
        Resource resource = TestResourceProvider.createResource(1L, platform, region);
        JsonObject requestBody = JsonObject.mapFrom(resource);
        requestBody.remove("resource_id");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        return requestBody;
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        JsonObject requestBody = composeResource();
        when(regionChecker.checkExistsByPlatform(22L, 11L)).thenReturn(Completable.complete());
        when(resourceChecker.submitCreate(requestBody)).thenReturn(Single.just(JsonObject.mapFrom(requestBody)));

        resourceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject("platform").getLong("platform_id")).isEqualTo(11L);
                    assertThat(result.getJsonObject("region").getLong("region_id")).isEqualTo(22L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOneRegionNotFound(VertxTestContext testContext) {
        composeResource();
        when(regionChecker.checkExistsByPlatform(22L, 11L)).thenReturn(Completable.error(NotFoundException::new));

        resourceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
