package at.uibk.dps.rm.service;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

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
        public ConcreteServiceProxy(Repository<ResourceType> repository, Stage.SessionFactory sessionFactory) {
            super(repository, ResourceType.class, sessionFactory);
        }
    }

    private ConcreteServiceProxy testClass;

    @Mock
    private Repository<ResourceType> testRepository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

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
        ResourceType entity = new ResourceType();
        entity.setTypeId(1L);
        entity.setResourceType("cloud");
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(testRepository).create(session, any(ResourceType.class));

        JsonObject data = new JsonObject("{\"resource_type\": \"cloud\"}");

        testClass.save(data)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("type_id")).isEqualTo(1L);
                testContext.completeNow();
        })));
    }

    @Test
    void saveAllEntities(VertxTestContext testContext) {
        CompletionStage<Void> completionStage = CompletionStages.voidFuture();
        doReturn(completionStage).when(testRepository).createAll(session, anyList());

        JsonArray data = new JsonArray("[{\"resource_type\": \"cloud\"}, {\"resource_type\": \"vm\"}, " +
                "{\"resource_type\": \"IoT\"}]");

        testClass.saveAll(data)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long typeId = 1L;
        ResourceType entity = new ResourceType();
        entity.setTypeId(typeId);
        entity.setResourceType("cloud");
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(testRepository).findById(session, typeId);

        testClass.findOne(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("type_id")).isEqualTo(1L);
                assertThat(result.getString("resource_type")).isEqualTo("cloud");
                testContext.completeNow();
        })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long typeId = 1L;
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(testRepository).findById(session, typeId);

        testClass.findOne(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityExists(VertxTestContext testContext) {
        long typeId = 1L;
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(typeId);
        resourceType.setResourceType("cloud");
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(resourceType);
        doReturn(completionStage).when(testRepository).findById(session, typeId);

        testClass.existsOneById(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityNotExists(VertxTestContext testContext) {
        long typeId = 1L;
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(testRepository).findById(session, typeId);

        testClass.existsOneById(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        ResourceType entity1 = new ResourceType();
        entity1.setTypeId(1L);
        entity1.setResourceType("cloud");
        ResourceType entity2 = new ResourceType();
        entity2.setTypeId(2L);
        entity2.setResourceType("vm");
        List<ResourceType> resultList = new ArrayList<>();
        resultList.add(entity1);
        resultList.add(entity2);
        CompletionStage<List<ResourceType>> completionStage = CompletionStages.completedFuture(resultList);
        doReturn(completionStage).when(testRepository).findAll(session);

        testClass.findAll()
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getString("resource_type")).isEqualTo("cloud");
                assertThat(result.getJsonObject(1).getString("resource_type")).isEqualTo("vm");
                testContext.completeNow();
        })));
    }

    @Test
    void updateEntity(VertxTestContext testContext) {
        long id = 1L;

        JsonObject data = new JsonObject("{\"type_id\": 1, \"resource_type\": \"cloud\"}");

        testClass.update(id, data)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void deleteEntity(VertxTestContext testContext) {
        long typeId = 1L;
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(testRepository).deleteById(session, typeId);

        testClass.delete(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }
}
