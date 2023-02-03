package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
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
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceProviderRegionHandlerTest {

    private ResourceProviderRegionHandler providerRegionHandler;

    @Mock
    private RegionService regionService;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        providerRegionHandler = new ResourceProviderRegionHandler(regionService);
    }

    @Test
    void checkFindAllByProviderFound(VertxTestContext testContext) {
        long providerId = 1L;
        Region region1 = TestObjectProvider.createRegion(1L, "us-east");
        Region region2 = TestObjectProvider.createRegion(2L, "us-west");
        Region region3 = TestObjectProvider.createRegion(3L, "eu-west");
        JsonArray regionJson = new JsonArray(List.of(JsonObject.mapFrom(region1), JsonObject.mapFrom(region2),
            JsonObject.mapFrom(region3)));

        when(rc.pathParam("id")).thenReturn(String.valueOf(providerId));
        when(regionService.findAllByProviderId(providerId)).thenReturn(Single.just(regionJson));

        providerRegionHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("region_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("region_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("region_id")).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
            );
    }

    @Test
    void checkFindAllByProviderEmpty(VertxTestContext testContext) {
        long providerId = 1L;
        JsonArray regionJson = new JsonArray(List.of());

        when(rc.pathParam("id")).thenReturn(String.valueOf(providerId));
        when(regionService.findAllByProviderId(providerId)).thenReturn(Single.just(regionJson));

        providerRegionHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
            );
    }

    @Test
    void checkFindAllByProviderNotFound(VertxTestContext testContext) {
        long providerId = 1L;
        Single<JsonArray> handler = new SingleHelper<JsonArray>().getEmptySingle();

        when(rc.pathParam("id")).thenReturn(String.valueOf(providerId));
        when(regionService.findAllByProviderId(providerId)).thenReturn(handler);

        providerRegionHandler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

}
