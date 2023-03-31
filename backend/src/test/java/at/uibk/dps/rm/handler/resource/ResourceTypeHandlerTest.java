package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.exception.UsedByOtherEntityException;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceTypeHandlerTest {

    private ResourceTypeHandler resourceTypeHandler;

    @Mock
    private ResourceTypeChecker resourceTypeChecker;

    @Mock
    private ResourceChecker resourceChecker;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceTypeHandler = new ResourceTypeHandler(resourceTypeChecker, resourceChecker);
    }

    @Test
    void checkDeleteEntityIsUsedFalse(VertxTestContext testContext) {
        long entityId = 1L;
        ResourceType entity = TestResourceProvider.createResourceType(entityId, "vm");

        when(resourceChecker.checkOneUsedByResourceType(entityId)).thenReturn(Completable.complete());

        resourceTypeHandler.checkDeleteEntityIsUsed(JsonObject.mapFrom((entity)))
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail(Arrays.toString(throwable.getStackTrace())))
            );
        testContext.completeNow();
    }

    @Test
    void checkDeleteEntityIsUsedTrue(VertxTestContext testContext) {
        long entityId = 1L;
        ResourceType entity = TestResourceProvider.createResourceType(entityId, "vm");

        when(resourceChecker.checkOneUsedByResourceType(entityId))
            .thenReturn(Completable.error(UsedByOtherEntityException::new));

        resourceTypeHandler.checkDeleteEntityIsUsed(JsonObject.mapFrom((entity)))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(UsedByOtherEntityException.class);
                    testContext.completeNow();
                })
            );
    }
}
