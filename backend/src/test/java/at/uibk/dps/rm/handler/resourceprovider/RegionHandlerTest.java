package at.uibk.dps.rm.handler.resourceprovider;


import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.util.JsonMapperConfig;
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

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class RegionHandlerTest {

    private RegionHandler regionHandler;

    @Mock
    private RegionChecker regionChecker;

    @Mock
    private ResourceProviderChecker providerChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        regionHandler = new RegionHandler(regionChecker, providerChecker);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject("{\"name\": \"us-east\", \"resource_provider\": {\"provider_id\": 1}}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(providerChecker.checkExistsOne(1L)).thenReturn(Completable.complete());
        when(regionChecker.checkForDuplicateEntity(requestBody)).thenReturn(Completable.complete());
        when(regionChecker.submitCreate(requestBody)).thenReturn(Single.just(requestBody));

        regionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getString("name")).isEqualTo("us-east");
                    assertThat(result.getJsonObject("resource_provider").getLong("provider_id"))
                        .isEqualTo(1L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOneRegionNotFound(VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject("{\"name\": \"us-east\", \"resource_provider\": {\"provider_id\": 1}}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(providerChecker.checkExistsOne(1L)).thenReturn(Completable.error(NotFoundException::new));
        when(regionChecker.checkForDuplicateEntity(requestBody)).thenReturn(Completable.complete());

        regionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void postOneRegionAlreadyExists(VertxTestContext testContext) {
        JsonObject requestBody = new JsonObject("{\"name\": \"us-east\", \"resource_provider\": {\"provider_id\": 1}}");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(providerChecker.checkExistsOne(1L)).thenReturn(Completable.complete());
        when(regionChecker.checkForDuplicateEntity(requestBody))
            .thenReturn(Completable.error(AlreadyExistsException::new));

        regionHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }
}
