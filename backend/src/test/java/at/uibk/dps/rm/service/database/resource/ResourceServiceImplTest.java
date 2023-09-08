package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestMetricProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestResourceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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
    private RegionRepository regionRepository;

    @Mock
    private MetricRepository metricRepository;

    @Mock
    private Stage.SessionFactory sessionFactory;

    @Mock
    private Stage.Session session;
    
    private SessionManager sessionManager;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceService = new ResourceServiceImpl(resourceRepository, regionRepository, metricRepository,
            sessionFactory);
    }

    @Test
    void findEntityExists(VertxTestContext testContext) {
        long resourceId = 1L;
        Resource entity = TestResourceProvider.createResource(resourceId);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(resourceRepository.findByIdAndFetch(sessionManager, resourceId))
            .thenReturn(Maybe.just(entity));

        resourceService.findOne(resourceId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("resource_id")).isEqualTo(1L);
                testContext.completeNow();
        })));
    }

    @Test
    void findEntityNotExists(VertxTestContext testContext) {
        long resourceId = 1L;

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(resourceRepository.findByIdAndFetch(sessionManager, resourceId))
            .thenReturn(Maybe.empty());

        resourceService.findOne(resourceId, testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(resourceRepository.findAllAndFetch(sessionManager))
            .thenReturn(Single.just(List.of(r1, r2)));

        resourceService.findAll(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllBySLOs(VertxTestContext testContext) {
        Metric availability = TestMetricProvider.createMetric(1L, "instance-type",
            TestMetricProvider.createMetricTypeString());
        ServiceLevelObjective slo1 = new ServiceLevelObjective("instance-type", ExpressionType.EQ,
            TestDTOProvider.createSLOValueList("t2.micro"));
        List<String> metrics = List.of("instance-type");
        Resource r1 = TestResourceProvider.createResourceLambda(1L);
        Resource r2 = TestResourceProvider.createResourceEC2(1L, "t2.micro");
        List<Long> regions = List.of();
        List<Long> resourceProviders = List.of();
        List<Long> resourceTypes = List.of();
        List<Long> platforms = List.of();
        List<Long> environments = List.of();
        SLORequest sloRequest = TestDTOProvider.createSLORequest(List.of(slo1));

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(resourceRepository.findAllBySLOs(sessionManager, metrics, environments, resourceTypes, platforms, regions,
            resourceProviders)).thenReturn(Single.just(List.of(r1, r2)));
        when(metricRepository.findByMetricAndIsSLO(eq(sessionManager), any(String.class)))
            .thenReturn(Maybe.just(availability));

        resourceService.findAllBySLOs(JsonObject.mapFrom(sloRequest), testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(1);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByResourceIds(VertxTestContext testContext) {
        List<Long> resourceIds = List.of(1L, 2L);
        Resource r1 = TestResourceProvider.createResource(1L);
        Resource r2 = TestResourceProvider.createResource(2L);

        sessionManager = SessionMockHelper.mockTransaction(sessionFactory, session);
        when(resourceRepository.findAllByResourceIdsAndFetch(sessionManager, resourceIds))
            .thenReturn(Single.just(List.of(r1, r2)));

        resourceService.findAllByResourceIds(resourceIds, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }
}
