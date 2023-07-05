package at.uibk.dps.rm.service.database;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        when(session.persist(entity)).thenReturn(CompletionStages.voidFuture());
        SessionMockHelper.mockTransaction(sessionFactory, session);

        testClass.save(JsonObject.mapFrom(entity))
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("type_id")).isEqualTo(1L);
                testContext.completeNow();
        })));
    }

    @Test
    void saveEntityToAccount() {
        assertThrows(UnsupportedOperationException.class, () -> testClass.saveToAccount(1L, new JsonObject()));
    }

    @Test
    void saveAllEntities(VertxTestContext testContext) {
        when(testRepository.createAll(eq(session), anyList())).thenReturn(CompletionStages.voidFuture());
        SessionMockHelper.mockTransaction(sessionFactory, session);
        JsonArray data = new JsonArray("[{\"resource_type\": \"container\"}, {\"resource_type\": \"vm\"}, " +
                "{\"resource_type\": \"faas\"}]");

        testClass.saveAll(data)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }

    private static Stream<Arguments> provideFindExists() {
        ResourceType entity = TestResourceProvider.createResourceTypeContainer(1L);
        ResourceType nullEntity = null;
        return Stream.of(
            Arguments.of(entity, true),
            Arguments.of(nullEntity, false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindExists")
    void findEntity(ResourceType entity, boolean resultIsNonNull, VertxTestContext testContext) {
        long typeId = 1L;
        when(testRepository.findById(session, typeId)).thenReturn(CompletionStages.completedFuture(entity));
        SessionMockHelper.mockSession(sessionFactory, session);

        testClass.findOne(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                if (resultIsNonNull) {
                    assertThat(result.getLong("type_id")).isEqualTo(1L);
                    assertThat(result.getString("resource_type")).isEqualTo("container");
                } else {
                    assertThat(result).isNull();
                }
                testContext.completeNow();
        })));
    }

    @ParameterizedTest
    @MethodSource("provideFindExists")
    void checkEntityExists(ResourceType entity, boolean resultIsNonNull, VertxTestContext testContext) {
        long typeId = 1L;
        when(testRepository.findById(session, typeId)).thenReturn(CompletionStages.completedFuture(entity));
        SessionMockHelper.mockSession(sessionFactory, session);

        testClass.existsOneById(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(resultIsNonNull);
                testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        ResourceType entity1 = TestResourceProvider.createResourceTypeContainer(1L);
        ResourceType entity2 = TestResourceProvider.createResourceTypeFaas(2L);
        when(testRepository.findAll(session)).thenReturn(CompletionStages.completedFuture(List.of(entity1, entity2)));
        SessionMockHelper.mockSession(sessionFactory, session);

        testClass.findAll()
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
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
        when(testRepository.findAllByAccountId(session, accountId))
            .thenReturn(CompletionStages.completedFuture(List.of(entity1, entity2)));
        SessionMockHelper.mockSession(sessionFactory, session);

        testClass.findAllByAccountId(accountId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
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

        when(testRepository.findById(session, id)).thenReturn(CompletionStages.completedFuture(existing));
        when(session.merge(updated)).thenReturn(CompletionStages.completedFuture(updated));
        SessionMockHelper.mockTransaction(sessionFactory, session);

        testClass.update(id, fields)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void updateEntityNotFound(VertxTestContext testContext) {
        long id = 1L;
        JsonObject fields = new JsonObject("{\"resource_type\": \"faas\"}");

        when(testRepository.findById(session, id)).thenReturn(CompletionStages.completedFuture(null));
        SessionMockHelper.mockTransaction(sessionFactory, session);

        testClass.update(id, fields)
            .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("ResourceType not found");
                testContext.completeNow();
            })));
    }

    @Test
    void delete(VertxTestContext testContext) {
        long id = 1L;
        ResourceType existing = TestResourceProvider.createResourceTypeContainer(id);

        when(testRepository.findById(session, id)).thenReturn(CompletionStages.completedFuture(existing));
        when(session.remove(existing)).thenReturn(CompletionStages.voidFuture());
        SessionMockHelper.mockTransaction(sessionFactory, session);

        testClass.delete(id)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void deleteNotFound(VertxTestContext testContext) {
        long id = 1L;

        when(testRepository.findById(session, id)).thenReturn(CompletionStages.completedFuture(null));
        SessionMockHelper.mockTransaction(sessionFactory, session);

        testClass.delete(id)
            .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                assertThat(throwable.getMessage()).isEqualTo("ResourceType not found");
                testContext.completeNow();
            })));
    }

    @Test
    void deleteFromAccount(VertxTestContext testContext) {
        long id = 1L, accountId = 2L;
        ResourceType existing = TestResourceProvider.createResourceTypeContainer(id);

        when(testRepository.findByIdAndAccountId(session, id, accountId))
            .thenReturn(CompletionStages.completedFuture(existing));
        when(session.remove(existing)).thenReturn(CompletionStages.voidFuture());
        SessionMockHelper.mockTransaction(sessionFactory, session);

        testClass.deleteFromAccount(accountId, id)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
            })));
    }

    @Test
    void deleteFromAccountNotFound(VertxTestContext testContext) {
        long id = 1L, accountId = 2L;

        when(testRepository.findByIdAndAccountId(session, id, accountId))
            .thenReturn(CompletionStages.completedFuture(null));
        SessionMockHelper.mockTransaction(sessionFactory, session);

        testClass.deleteFromAccount(accountId, id)
            .onComplete(testContext.failing(throwable -> testContext.verify(() -> {
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
