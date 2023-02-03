package at.uibk.dps.rm.handler.resourceprovider;


import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.util.JsonMapperConfig;
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

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RegionHandlerTest {

    private RegionHandler regionHandler;

    @Mock
    private RegionService regionService;

    @Mock
    private ResourceProviderService providerService;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        regionHandler = new RegionHandler(regionService, providerService);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"name\": \"us-east\", \"resource_provider\": {\"provider_id\": 1}}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(providerService.existsOneById(1L)).thenReturn(Single.just(true));
        when(regionService.existsOneByNameAndProviderId("us-east", 1L)).thenReturn(Single.just(false));
        when(regionService.save(jsonObject)).thenReturn(Single.just(jsonObject));

        regionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getString("name")).isEqualTo("us-east");
                    assertThat(result.getJsonObject("resource_provider").getLong("provider_id"))
                        .isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );
    }

    @Test
    void postOneRegionNotFound(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"name\": \"us-east\", \"resource_provider\": {\"provider_id\": 1}}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(providerService.existsOneById(1L)).thenReturn(Single.just(false));
        when(regionService.existsOneByNameAndProviderId("us-east", 1L)).thenReturn(Single.just(false));

        regionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postOneRegionAlreadyExists(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"name\": \"us-east\", \"resource_provider\": {\"provider_id\": 1}}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(providerService.existsOneById(1L)).thenReturn(Single.just(true));
        when(regionService.existsOneByNameAndProviderId("us-east", 1L)).thenReturn(Single.just(true));

        regionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }
}
