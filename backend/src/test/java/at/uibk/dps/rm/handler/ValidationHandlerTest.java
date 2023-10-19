package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.service.rxjava3.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceTypeService;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
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

/**
 * Implements tests for the {@link ValidationHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ValidationHandlerTest {

    /**
     * Implements a concrete class of the {@link ValidationHandler} class.
     */
    static class ConcreteValidationHandler extends ValidationHandler {
        /**
         * Create an instance from the service.
         *
         * @param service the service
         */
        protected ConcreteValidationHandler(DatabaseServiceInterface service) {
            super(service);
        }
    }

    private ConcreteValidationHandler testClass;

    @Mock
    private ResourceTypeService resourceTypeService;

    @Mock
    RoutingContext rc;

    private long entityId;
    private Account account;
    private ResourceType entity1, entity2;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        testClass = new ConcreteValidationHandler(resourceTypeService);
        entityId = 1L;
        account = TestAccountProvider.createAccount(10L);
        entity1 = TestResourceProvider.createResourceType(entityId, "cloud");
        entity2 = TestResourceProvider.createResourceType(2L, "vm");
    }

    @Test
    void deleteOne(VertxTestContext testContext) {
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.delete(entityId)).thenReturn(Completable.complete());

        testClass.deleteOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void deleteOneFromAccount(VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.deleteFromAccount(10L, entityId)).thenReturn(Completable.complete());

        testClass.deleteOneFromAccount(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void getOne(VertxTestContext testContext) {
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.findOne(entityId)).thenReturn(Single.just(JsonObject.mapFrom(entity1)));

        testClass.getOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("type_id")).isEqualTo(entityId);
                    assertThat(result.getString("resource_type")).isEqualTo("cloud");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getOneFromAccount(VertxTestContext testContext) {
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.findOneByIdAndAccountId(entityId, 10L))
            .thenReturn(Single.just(JsonObject.mapFrom(entity1)));

        testClass.getOneFromAccount(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("type_id")).isEqualTo(entityId);
                    assertThat(result.getString("resource_type")).isEqualTo("cloud");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getAll(VertxTestContext testContext) {
        JsonArray resultJson = new JsonArray(List.of(JsonObject.mapFrom(entity1), JsonObject.mapFrom(entity2)));

        when(resourceTypeService.findAll()).thenReturn(Single.just(resultJson));

        testClass.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("type_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void getAllFromAccount(VertxTestContext testContext) {
        JsonArray resultJson = new JsonArray(List.of(JsonObject.mapFrom(entity1), JsonObject.mapFrom(entity2)));

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(resourceTypeService.findAllByAccountId(10L)).thenReturn(Single.just(resultJson));

        testClass.getAllFromAccount(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.getJsonObject(0).getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getJsonObject(1).getLong("type_id")).isEqualTo(2L);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOne(VertxTestContext testContext) {
        JsonObject requestBody = JsonObject.mapFrom(entity1);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(resourceTypeService.save(requestBody)).thenReturn(Single.just(requestBody));

        testClass.postOne(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getString("resource_type")).isEqualTo("cloud");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postOneToAccount(VertxTestContext testContext) {
        JsonObject requestBody = JsonObject.mapFrom(entity1);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(resourceTypeService.saveToAccount(10L, requestBody)).thenReturn(Single.just(requestBody));

        testClass.postOneToAccount(rc)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getString("resource_type")).isEqualTo("cloud");
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }

    @Test
    void postAll(VertxTestContext testContext) {
        JsonArray resultJson = new JsonArray(List.of(JsonObject.mapFrom(entity1), JsonObject.mapFrom(entity2)));

        RoutingContextMockHelper.mockBody(rc, resultJson);
        when(resourceTypeService.saveAll(resultJson)).thenReturn(Completable.complete());

        testClass.postAll(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void updateOne(VertxTestContext testContext) {
        JsonObject requestBody = JsonObject.mapFrom(entity1);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.update(entityId, requestBody)).thenReturn(Completable.complete());

        testClass.updateOne(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void updateOneOwned(VertxTestContext testContext) {
        JsonObject requestBody = JsonObject.mapFrom(entity1);

        RoutingContextMockHelper.mockBody(rc, requestBody);
        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(entityId));
        when(resourceTypeService.updateOwned(entityId, account.getAccountId(), requestBody))
            .thenReturn(Completable.complete());

        testClass.updateOneOwned(rc)
            .blockingSubscribe(() -> {},
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }
}
