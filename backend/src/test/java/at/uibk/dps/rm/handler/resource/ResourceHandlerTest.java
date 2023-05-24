package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
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
    private ResourceTypeChecker resourceTypeChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceHandler = new ResourceHandler(resourceChecker, resourceTypeChecker);
    }

    @Test
    void postOneValid(VertxTestContext testContext) {
        ResourceType rt = TestResourceProvider.createResourceType(1L, "vm");
        Resource resource = TestResourceProvider.createResource(1L, rt);
        JsonObject requestBody = JsonObject.mapFrom(resource);
        requestBody.remove("resource_id");

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(resourceTypeChecker.checkExistsOne(1L)).thenReturn(Completable.complete());
        when(resourceChecker.submitCreate(requestBody)).thenReturn(Single.just(JsonObject.mapFrom(resource)));

        resourceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getJsonObject("resource_type").getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getBoolean("is_self_managed")).isEqualTo(false);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOneResourceTypeNotFound(VertxTestContext testContext) {
        JsonObject jsonObject = new JsonObject("{\"resource_type\": {\"type_id\": 1}, \"is_self_managed\": false}");

        RoutingContextMockHelper.mockBody(rc, jsonObject);
        when(resourceTypeChecker.checkExistsOne(1L)).thenReturn(Completable.error(NotFoundException::new));

        resourceHandler.postOne(rc)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
