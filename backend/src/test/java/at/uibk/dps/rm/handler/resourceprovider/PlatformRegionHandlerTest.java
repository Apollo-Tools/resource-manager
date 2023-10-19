package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link PlatformRegionHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class PlatformRegionHandlerTest {

    private PlatformRegionHandler platformRegionHandler;

    @Mock
    private RegionService regionService;

    @Mock
    private RoutingContext rc;

    private Region r1, r2;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        platformRegionHandler = new PlatformRegionHandler(regionService);
        r1 = TestResourceProviderProvider.createRegion(1L, "us-east-1");
        r2 = TestResourceProviderProvider.createRegion(2L, "us-east-2");
    }

    @Test
    void getAll(VertxTestContext testContext) {
        long platformId = 1L;
        JsonArray jsonResult = new JsonArray(List.of(JsonObject.mapFrom(r1), JsonObject.mapFrom(r2)));

        when(rc.pathParam("id")).thenReturn(String.valueOf(platformId));
        when(regionService.findAllByPlatformId(platformId)).thenReturn(Single.just(jsonResult));

        platformRegionHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.getJsonObject(0).getLong("region_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("region_id")).isEqualTo(2L);
                testContext.completeNow();
            }));
    }
}
