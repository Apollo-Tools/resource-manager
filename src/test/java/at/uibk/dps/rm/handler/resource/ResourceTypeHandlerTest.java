package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.exception.UsedByOtherEntityException;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceTypeHandlerTest {

    private ResourceTypeHandler resourceTypeHandler;

    @Mock
    private ResourceTypeService resourceTypeService;

    @Mock
    private ResourceService resourceService;

    @BeforeEach
    void initTest() {
        resourceTypeHandler = new ResourceTypeHandler(resourceTypeService, resourceService);
    }

    @Test
    void checkDeleteEntityIsUsedFalse(VertxTestContext testContext) {
        long entityId = 1L;
        ResourceType entity = new ResourceType();
        entity.setTypeId(entityId);
        entity.setResource_type("cloud");

        when(resourceService.existsOneByResourceType(entityId)).thenReturn(Single.just(false));

        resourceTypeHandler.checkDeleteEntityIsUsed(JsonObject.mapFrom((entity)))
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(resourceService).existsOneByResourceType(entityId);
        testContext.completeNow();
    }

    @Test
    void checkDeleteEntityIsUsedTrue(VertxTestContext testContext) {
        long entityId = 1L;
        ResourceType entity = new ResourceType();
        entity.setTypeId(entityId);
        entity.setResource_type("cloud");

        when(resourceService.existsOneByResourceType(entityId)).thenReturn(Single.just(true));

        resourceTypeHandler.checkDeleteEntityIsUsed(JsonObject.mapFrom((entity)))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UsedByOtherEntityException.class);
                    testContext.completeNow();
                })
            );
    }
}
