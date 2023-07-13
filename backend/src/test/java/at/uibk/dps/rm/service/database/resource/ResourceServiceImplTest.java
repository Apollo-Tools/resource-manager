package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.resource.ResourceTypeEnum;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link ResourceServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ResourceServiceImplTest {

    private ResourceService resourceService;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceService = new ResourceServiceImpl(resourceRepository, metricRepository, sessionFactory);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long resourceId = 1L;
        Resource entity = TestResourceProvider.createResource(resourceId);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(resourceRepository.findByIdAndFetch(session, resourceId))
            .thenReturn(CompletionStages.completedFuture(entity));

        resourceService.findOne(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonArray("metric_values")).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long resourceId = 1L;

        SessionMockHelper.mockSession(sessionFactory, session);
        when(resourceRepository.findByIdAndFetch(session, resourceId))
            .thenReturn(CompletionStages.completedFuture(null));

        resourceService.findOne(resourceId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isNull();
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityByResourceTypeExists(VertxTestContext testContext) {
        long typeId = 1L;
        Resource entity = new Resource();

        SessionMockHelper.mockSession(sessionFactory, session);
        when(resourceRepository.findByResourceType(session, typeId))
            .thenReturn(CompletionStages.completedFuture(List.of(entity)));

        resourceService.existsOneByResourceType(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(true);
                testContext.completeNow();
        })));
    }

    @Test
    void checkEntityNotExists(VertxTestContext testContext) {
        long typeId = 1L;

        SessionMockHelper.mockSession(sessionFactory, session);
        when(resourceRepository.findByResourceType(session, typeId))
            .thenReturn(CompletionStages.completedFuture(List.of()));

        resourceService.existsOneByResourceType(typeId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(false);
                testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(resourceRepository.findAllAndFetch(session))
            .thenReturn(CompletionStages.completedFuture(List.of(r1, r2)));

        resourceService.findAll()
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Disabled
    @Test
    void findAllBySLOs(VertxTestContext testContext) {
        Metric availability = TestMetricProvider.createMetric(1L, "availabiliy");
        List<String> metrics = List.of("availability");
        Resource entity1 = TestResourceProvider.createResource(1L);
        List<Long> regions = List.of();
        List<Long> resourceProviders = List.of();
        List<Long> resourceTypes = List.of();
        List<Long> platforms = List.of();
        List<Long> environments = List.of();
        SLORequest sloRequest = TestDTOProvider.createSLORequest();

        SessionMockHelper.mockSession(sessionFactory, session);
        when(resourceRepository.findAllBySLOs(session, metrics, environments, resourceTypes, platforms, regions,
            resourceProviders)).thenReturn(CompletionStages.completedFuture(List.of(entity1)));
        when(metricRepository.findByMetric(eq(session), any(String.class)))
            .thenReturn(CompletionStages.completedFuture(availability));

        resourceService.findAllBySLOs(JsonObject.mapFrom(sloRequest))
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(1);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByEnsembleId(VertxTestContext testContext) {
        long ensembleId = 1L;
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(resourceRepository.findAllByEnsembleId(session, ensembleId))
            .thenReturn(CompletionStages.completedFuture(List.of(r1, r2)));

        resourceService.findAllByEnsembleId(ensembleId)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void existsAllByIdsAndResourceTypes(boolean allExist, VertxTestContext testContext) {
        Set<Long> resourceIds = Set.of(1L, 2L);
        List<String> resourceTypes = List.of(ResourceTypeEnum.FAAS.getValue());
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);
        List<Resource> resources = List.of(r1, r2);
        if (!allExist) {
            resources = List.of(r2);
        }

        SessionMockHelper.mockSession(sessionFactory, session);
        when(resourceRepository.findAllByResourceIdsAndResourceTypes(session, resourceIds, resourceTypes))
            .thenReturn(CompletionStages.completedFuture(resources));

        resourceService.existsAllByIdsAndResourceTypes(resourceIds, resourceTypes)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result).isEqualTo(allExist);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByResourceIds(VertxTestContext testContext) {
        List<Long> resourceIds = List.of(1L, 2L);
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);

        SessionMockHelper.mockSession(sessionFactory, session);
        when(resourceRepository.findAllByResourceIdsAndFetch(session, resourceIds))
            .thenReturn(CompletionStages.completedFuture(List.of(r1, r2)));

        resourceService.findAllByResourceIds(resourceIds)
            .onComplete(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }
}
