package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.testutil.SingleHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProviderProvider;
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
public class RegionCheckerTest {

    private RegionChecker regionChecker;

    @Mock
    private RegionService regionService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        regionChecker = new RegionChecker(regionService);
    }

    @Test
    void checkFindAllByProviderFound(VertxTestContext testContext) {
        long providerId = 1L;
        Region region1 = TestResourceProviderProvider.createRegion(1L, "us-east");
        Region region2 = TestResourceProviderProvider.createRegion(2L, "us-west");
        Region region3 = TestResourceProviderProvider.createRegion(3L, "eu-west");
        JsonArray regionJson = new JsonArray(List.of(JsonObject.mapFrom(region1), JsonObject.mapFrom(region2),
            JsonObject.mapFrom(region3)));

        when(regionService.findAllByProviderId(providerId)).thenReturn(Single.just(regionJson));

        regionChecker.checkFindAllByProvider(providerId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(3);
                    assertThat(result.getJsonObject(0).getLong("region_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("region_id")).isEqualTo(2L);
                    assertThat(result.getJsonObject(2).getLong("region_id")).isEqualTo(3L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByProviderEmpty(VertxTestContext testContext) {
        long providerId = 1L;
        JsonArray regionJson = new JsonArray(List.of());

        when(regionService.findAllByProviderId(providerId)).thenReturn(Single.just(regionJson));

        regionChecker.checkFindAllByProvider(providerId)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(0);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkFindAllByProviderNotFound(VertxTestContext testContext) {
        long providerId = 1L;
        Single<JsonArray> handler = SingleHelper.getEmptySingle();

        when(regionService.findAllByProviderId(providerId)).thenReturn(handler);

        regionChecker.checkFindAllByProvider(providerId)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkForDuplicateEntityFalse(VertxTestContext testContext) {
        long providerId = 2L;
        String name = "eu-west";
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(providerId);
        Region region = TestResourceProviderProvider.createRegion(1L, name, resourceProvider);

        when(regionService.existsOneByNameAndProviderId(name, providerId)).thenReturn(Single.just(false));

        regionChecker.checkForDuplicateEntity(JsonObject.mapFrom(region))
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        verify(regionService).existsOneByNameAndProviderId(name, providerId);
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityTrue(VertxTestContext testContext) {
        long providerId = 2L;
        String name = "eu-west";
        ResourceProvider resourceProvider = TestResourceProviderProvider.createResourceProvider(providerId);
        Region region = TestResourceProviderProvider.createRegion(1L, name, resourceProvider);

        when(regionService.existsOneByNameAndProviderId(name, providerId)).thenReturn(Single.just(true));

        regionChecker.checkForDuplicateEntity(JsonObject.mapFrom(region))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }
}
