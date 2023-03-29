package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
import at.uibk.dps.rm.testutil.TestObjectProvider;
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
public class ResourceTypeCheckerTest {

    ResourceTypeChecker resourceTypeChecker;

    @Mock
    ResourceTypeService resourceTypeService;

    @BeforeEach
    void initTest() {
        resourceTypeChecker = new ResourceTypeChecker(resourceTypeService);
    }

    @Test
    void checkForDuplicateEntityNotExists(VertxTestContext testContext) {
        String resourceTypeName = "cloud";
        ResourceType resourceType = TestObjectProvider.createResourceType(1L, resourceTypeName);
        JsonObject entity = JsonObject.mapFrom(resourceType);

        when(resourceTypeService.existsOneByResourceType(resourceTypeName)).thenReturn(Single.just(false));

        resourceTypeChecker.checkForDuplicateEntity(entity)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );

        verify(resourceTypeService).existsOneByResourceType(resourceTypeName);
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityExists(VertxTestContext testContext) {
        String resourceTypeName = "cloud";
        ResourceType resourceType = TestObjectProvider.createResourceType(1L, resourceTypeName);
        JsonObject entity = JsonObject.mapFrom(resourceType);

        when(resourceTypeService.existsOneByResourceType(resourceTypeName)).thenReturn(Single.just(true));

        resourceTypeChecker.checkForDuplicateEntity(entity)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateNoDuplicateWithResourceTypeNotExists(VertxTestContext testContext) {
        String resourceTypeName = "cloud";
        ResourceType resourceType = TestObjectProvider.createResourceType(1L, resourceTypeName);
        JsonObject entity = JsonObject.mapFrom(resourceType);

        when(resourceTypeService.existsOneByResourceType(resourceTypeName)).thenReturn(Single.just(false));

        resourceTypeChecker.checkUpdateNoDuplicate(entity, entity)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getString("resource_type")).isEqualTo(resourceTypeName);
                    verify(resourceTypeService).existsOneByResourceType(resourceTypeName);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void checkUpdateNoDuplicateWithResourceTypeExists(VertxTestContext testContext) {
        String resourceTypeName = "cloud";
        ResourceType resourceType = TestObjectProvider.createResourceType(1L, resourceTypeName);
        JsonObject entity = JsonObject.mapFrom(resourceType);

        when(resourceTypeService.existsOneByResourceType(resourceTypeName)).thenReturn(Single.just(true));

        resourceTypeChecker.checkUpdateNoDuplicate(entity, entity)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateNoDuplicateWithoutResource(VertxTestContext testContext) {
        String resourceTypeName = "cloud";
        ResourceType resourceType = TestObjectProvider.createResourceType(1L, resourceTypeName);
        JsonObject body = JsonObject.mapFrom(resourceType);
        body.remove("resource_type");
        JsonObject entity = JsonObject.mapFrom(resourceType);

        resourceTypeChecker.checkUpdateNoDuplicate(body, entity)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getString("resource_type")).isEqualTo(resourceTypeName);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

}
