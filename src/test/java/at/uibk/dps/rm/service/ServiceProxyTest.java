package at.uibk.dps.rm.service;

import at.uibk.dps.rm.entity.model.ResourceType;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
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
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceProxyTest {

    static class ConcreteServiceProxy extends ServiceProxy<ResourceType> {
        public ConcreteServiceProxy(Repository<ResourceType> repository) {
            super(repository, ResourceType.class);
        }
    }

    private ConcreteServiceProxy testClass;

    @Mock
    Repository<ResourceType> testRepository;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        testClass = new ConcreteServiceProxy(testRepository);
    }

    @Test
    void saveEntity(VertxTestContext testContext) {
        ResourceType entity = new ResourceType();
        entity.setTypeId(1L);
        entity.setResource_type("cloud");
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(testRepository).create(any(ResourceType.class));

        JsonObject data = new JsonObject("{\"resource_type\": \"cloud\"}");

        testClass.save(data)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("type_id")).isEqualTo(1L);
                verify(testRepository, times(1)).create(any(ResourceType.class));
                testContext.completeNow();
        })));
    }

    @Test
    void saveAllEntities(VertxTestContext testContext) {
        CompletionStage<Void> completionStage = CompletionStages.voidFuture();
        doReturn(completionStage).when(testRepository).createAll(anyList());

        JsonArray data = new JsonArray("[{\"resource_type\": \"cloud\"}, {\"resource_type\": \"vm\"}, " +
                "{\"resource_type\": \"IoT\"}]");

        testClass.saveAll(data)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(testRepository, times(1)).createAll(anyList());
                testContext.completeNow();
        })));
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long typeId = 1L;
        ResourceType entity = new ResourceType();
        entity.setTypeId(typeId);
        entity.setResource_type("cloud");
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(entity);
        doReturn(completionStage).when(testRepository).findById(typeId);

        testClass.findOne(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("type_id")).isEqualTo(1L);
                assertThat(result.getString("resource_type")).isEqualTo("cloud");
                verify(testRepository, times(1)).findById(typeId);
                testContext.completeNow();
        })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long typeId = 1L;
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(testRepository).findById(typeId);

        testClass.findOne(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(testRepository, times(1)).findById(typeId);
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityExists(VertxTestContext testContext) {
        long typeId = 1L;
        ResourceType resourceType = new ResourceType();
        resourceType.setTypeId(typeId);
        resourceType.setResource_type("cloud");
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(resourceType);
        doReturn(completionStage).when(testRepository).findById(typeId);

        testClass.existsOneById(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                verify(testRepository, times(1)).findById(typeId);
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityNotExists(VertxTestContext testContext) {
        long typeId = 1L;
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(testRepository).findById(typeId);

        testClass.existsOneById(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                verify(testRepository, times(1)).findById(typeId);
                testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        ResourceType entity1 = new ResourceType();
        entity1.setTypeId(1L);
        entity1.setResource_type("cloud");
        ResourceType entity2 = new ResourceType();
        entity2.setTypeId(2L);
        entity2.setResource_type("vm");
        List<ResourceType> resultList = new ArrayList<>();
        resultList.add(entity1);
        resultList.add(entity2);
        CompletionStage<List<ResourceType>> completionStage = CompletionStages.completedFuture(resultList);
        doReturn(completionStage).when(testRepository).findAll();

        testClass.findAll()
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getString("resource_type")).isEqualTo("cloud");
                assertThat(result.getJsonObject(1).getString("resource_type")).isEqualTo("vm");
                verify(testRepository, times(1)).findAll();
                testContext.completeNow();
        })));
    }

    @Test
    void updateEntity(VertxTestContext testContext) {
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(testRepository).update(any(ResourceType.class));

        JsonObject data = new JsonObject("{\"type_id\": 1, \"resource_type\": \"cloud\"}");

        testClass.update(data)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(testRepository, times(1)).update(any(ResourceType.class));
                testContext.completeNow();
        })));
    }

    @Test
    void deleteEntity(VertxTestContext testContext) {
        long typeId = 1L;
        CompletionStage<ResourceType> completionStage = CompletionStages.completedFuture(null);
        doReturn(completionStage).when(testRepository).deleteById(typeId);

        testClass.delete(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                verify(testRepository, times(1)).deleteById(typeId);
                testContext.completeNow();
        })));
    }
}
