package at.uibk.dps.rm.service.database;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage.Session;
import org.hibernate.reactive.stage.Stage.SessionFactory;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link DatabaseServiceProxy} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DatabaseServiceProxyTest {

    /**
     * Implements a concrete class of the {@link DatabaseServiceProxy} class.
     */
    static class ConcreteServiceProxy extends DatabaseServiceProxy<ResourceType> {
        /**
         * Create an instance from the repository.
         *
         * @param repository the repository
         */
        public ConcreteServiceProxy(Repository<ResourceType> repository, SessionFactory sessionFactory) {
            super(repository, ResourceType.class, sessionFactory);
        }
    }

    private ConcreteServiceProxy testClass;

    @Mock
    private Repository<ResourceType> testRepository;

    @Mock
    private SessionFactory sessionFactory;

    @Mock
    private Session session;

    private SessionManager sessionManager;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        testClass = new ConcreteServiceProxy(testRepository, sessionFactory);
    }

    @Test
    void getServiceProxyAddress() {
        String expected = "resourcetype-service-address";

        String result = testClass.getServiceProxyAddress();

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void saveEntity(VertxTestContext testContext) {
        ResourceType entity = TestResourceProvider.createResourceTypeContainer(1L);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(session.persist(entity)).thenReturn(CompletionStages.voidFuture());

        testClass.save(JsonObject.mapFrom(entity), testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("type_id")).isEqualTo(1L);
                testContext.completeNow();
        })));
    }

    @Test
    void saveEntityToAccount(VertxTestContext testContext) {
        testClass.saveToAccount(1L, new JsonObject(),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(UnsupportedOperationException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void saveAllEntities(VertxTestContext testContext) {
        JsonArray data = new JsonArray("[{\"resource_type\": \"container\"}, {\"resource_type\": \"vm\"}, " +
            "{\"resource_type\": \"faas\"}]");

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(testRepository.createAll(eq(sessionManager), anyList())).thenReturn(Completable.complete());

        testClass.saveAll(data, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void findEntity(VertxTestContext testContext) {
        ResourceType entity = TestResourceProvider.createResourceTypeContainer(1L);
        long typeId = 1L;

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(session.find(ResourceType.class, typeId)).thenReturn(CompletionStages.completedFuture(entity));

        testClass.findOne(typeId, testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(result.getLong("type_id")).isEqualTo(1L);
            assertThat(result.getString("resource_type")).isEqualTo("container");
            testContext.completeNow();
        })));
    }

    @Test
    void findEntityNotFound(VertxTestContext testContext) {
        long typeId = 1L;

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(session.find(ResourceType.class, typeId)).thenReturn(CompletionStages.nullFuture());

        testClass.findOne(typeId, testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void findOnyByIdAndAccountId(VertxTestContext testContext) {
        long typeId = 1L;
        long accountId = 2L;
        ResourceType entity = TestResourceProvider.createResourceTypeContainer(1L);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(testRepository.findByIdAndAccountId(sessionManager, typeId, accountId))
            .thenReturn(Maybe.just(entity));

        testClass.findOneByIdAndAccountId(typeId, accountId,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("type_id")).isEqualTo(1L);
                assertThat(result.getString("resource_type")).isEqualTo("container");
                testContext.completeNow();
        })));
    }

    @Test
    void findOnyByIdAndAccountIdNotFound(VertxTestContext testContext) {
        long typeId = 1L;
        long accountId = 2L;

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(testRepository.findByIdAndAccountId(sessionManager, typeId, accountId))
            .thenReturn(Maybe.empty());

        testClass.findOneByIdAndAccountId(typeId, accountId, testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        ResourceType entity1 = TestResourceProvider.createResourceTypeContainer(1L);
        ResourceType entity2 = TestResourceProvider.createResourceTypeFaas(2L);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(testRepository.findAll(sessionManager)).thenReturn(Single.just(List.of(entity1, entity2)));

        testClass.findAll(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getString("resource_type")).isEqualTo("container");
                assertThat(result.getJsonObject(1).getString("resource_type")).isEqualTo("faas");
                testContext.completeNow();
        })));
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        long accountId = 1L;
        ResourceType entity1 = TestResourceProvider.createResourceTypeContainer(1L);
        ResourceType entity2 = TestResourceProvider.createResourceTypeFaas(2L);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(testRepository.findAllByAccountId(sessionManager, accountId))
            .thenReturn(Single.just(List.of(entity1, entity2)));

        testClass.findAllByAccountId(accountId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getString("resource_type")).isEqualTo("container");
                assertThat(result.getJsonObject(1).getString("resource_type")).isEqualTo("faas");
                testContext.completeNow();
        })));
    }

    @Test
    void updateEntityExists(VertxTestContext testContext) {
        long id = 1L;
        ResourceType existing = TestResourceProvider.createResourceTypeContainer(id);
        ResourceType updated = TestResourceProvider.createResourceTypeFaas(id);
        JsonObject fields = new JsonObject("{\"resource_type\": \"faas\"}");

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(session.find(ResourceType.class, id)).thenReturn(CompletionStages.completedFuture(existing));
        when(session.merge(updated)).thenReturn(CompletionStages.completedFuture(updated));

        testClass.update(id, fields, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void updateEntityNotFound(VertxTestContext testContext) {
        long id = 1L;
        JsonObject fields = new JsonObject("{\"resource_type\": \"faas\"}");

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(session.find(ResourceType.class, id)).thenReturn(CompletionStages.nullFuture());

        testClass.update(id, fields, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("ResourceType not found");
                testContext.completeNow();
            })));
    }

    @Test
    void updateOwnedToAccount(VertxTestContext testContext) {
        testClass.updateOwned(1L, 2L, new JsonObject(),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(UnsupportedOperationException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void delete(VertxTestContext testContext) {
        long id = 1L;
        ResourceType existing = TestResourceProvider.createResourceTypeContainer(id);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(session.find(ResourceType.class, id)).thenReturn(CompletionStages.completedFuture(existing));
        when(session.remove(existing)).thenReturn(CompletionStages.voidFuture());

        testClass.delete(id, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void deleteNotFound(VertxTestContext testContext) {
        long id = 1L;

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(session.find(ResourceType.class, id)).thenReturn(CompletionStages.nullFuture());

        testClass.delete(id, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("ResourceType not found");
                testContext.completeNow();
            })));
    }

    @Test
    void deleteFromAccount(VertxTestContext testContext) {
        long id = 1L, accountId = 2L;
        ResourceType existing = TestResourceProvider.createResourceTypeContainer(id);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(testRepository.findByIdAndAccountId(sessionManager, id, accountId))
            .thenReturn(Maybe.just(existing));
        when(session.remove(existing)).thenReturn(CompletionStages.voidFuture());

        testClass.deleteFromAccount(accountId, id, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void deleteFromAccountNotFound(VertxTestContext testContext) {
        long id = 1L, accountId = 2L;

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(testRepository.findByIdAndAccountId(sessionManager, id, accountId))
            .thenReturn(Maybe.empty());

        testClass.deleteFromAccount(accountId, id, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("ResourceType not found");
                testContext.completeNow();
            })));
    }

    @ParameterizedTest
    @CsvSource({
        "3, 10, 10",
        "3, , 3"
    })
    void testUpdateNonNullValue(Integer oldValue, Integer newValue, Integer expected) {
        int result = testClass.updateNonNullValue(oldValue, newValue);

        assertThat(result).isEqualTo(expected);
    }
}
