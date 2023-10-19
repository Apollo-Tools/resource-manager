package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.K8sNode;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.service.database.util.*;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.mockprovider.DatabaseUtilMockprovider;
import at.uibk.dps.rm.testutil.objectprovider.*;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private JsonObject data;
    private Region reg1;
    private Platform p1;
    private Resource r1, r2;
    private SubResource sr1, sr2;
    private MainResource cr1;
    private K8sMonitoringData monitoringData;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceService = new ResourceServiceImpl(resourceRepository, regionRepository, metricRepository,
            smProvider);
        data = new JsonObject("{\"name\": \"new_r\", \"region\": {\"region_id\": 1}, \"platform\": " +
            "{\"platform_id\":  2}}");
        reg1 = TestResourceProviderProvider.createRegion(1L, "us-east");
        p1 = TestPlatformProvider.createPlatformFaas(2L, "lambda");
        r1 = TestResourceProvider.createResource(1L);
        r2 = TestResourceProvider.createResource(2L);
        cr1 = TestResourceProvider.createClusterWithoutNodes(3L, "cluster");
        sr1 = TestResourceProvider.createSubResource(4L, "subresource1", cr1);
        sr2 = TestResourceProvider.createSubResource(5L, "subresource2", cr1);
        K8sNode k8sn1 = TestMonitoringDataProvider.createK8sNode("n1", 10.0, 8.75,
            1000, 500, 10000, 5000);
        V1Namespace namespace = TestMonitoringDataProvider.createV1Namespace("default");
        monitoringData = new K8sMonitoringData(List.of(k8sn1), List.of(namespace));
    }

    @ParameterizedTest
    @ValueSource(strings = {"main", "sub"})
    void findOneMainResource(String type, VertxTestContext testContext) {
        Resource resource = type.equals("main") ? cr1 : sr1;
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(resourceRepository.findByIdAndFetch(sessionManager, resource.getResourceId())).thenReturn(Maybe.just(resource));

        resourceService.findOne(resource.getResourceId(), testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("resource_id")).isEqualTo(resource.getResourceId());
                assertThat(result.containsKey("sub_resources")).isEqualTo(type.equals("main"));
            assertThat(result.containsKey("main_metric_values")).isEqualTo(type.equals("sub"));
                testContext.completeNow();
        })));
    }

    @Test
    void findOneNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(resourceRepository.findByIdAndFetch(sessionManager, r1.getResourceId())).thenReturn(Maybe.empty());

        resourceService.findOne(r1.getResourceId(), testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findAllAndFetch(sessionManager)).thenReturn(Single.just(List.of(r1, r2)));

        resourceService.findAll(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllBySLOs(VertxTestContext testContext) {
        ServiceLevelObjective slo1 = new ServiceLevelObjective("instance-type", ExpressionType.EQ,
            TestDTOProvider.createSLOValueList("t2.micro"));
        SLORequest sloRequest = TestDTOProvider.createSLORequest(List.of(slo1));

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        try (MockedConstruction<SLOUtility> ignored = DatabaseUtilMockprovider.mockSLOUtilityFindAndFilterResources(sessionManager, List.of(r1, r2))) {
            resourceService.findAllBySLOs(JsonObject.mapFrom(sloRequest), testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
        }
    }

    @Test
    void findAllSubResources(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findAllSubresources(sessionManager, cr1.getResourceId()))
            .thenReturn(Single.just(List.of(sr1, sr2)));

        resourceService.findAllSubResources(cr1.getResourceId(),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(4L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(5L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByResourceIds(VertxTestContext testContext) {
        List<Long> resourceIds = List.of(1L, 2L);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findAllByResourceIdsAndFetch(sessionManager, resourceIds))
            .thenReturn(Single.just(List.of(r1, r2)));

        resourceService.findAllByResourceIds(resourceIds, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void save(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findByName(sessionManager, "new_r")).thenReturn(Maybe.empty());
        when(regionRepository.findByRegionIdAndPlatformId(sessionManager, reg1.getRegionId(), p1.getPlatformId()))
            .thenReturn(Maybe.just(reg1));
        when(sessionManager.find(Platform.class, p1.getPlatformId())).thenReturn(Maybe.just(p1));
        when(sessionManager.persist(argThat((Resource res) -> res.getName().equals("new_r") &&
                res.getMain().getRegion().equals(reg1) && res.getMain().getPlatform().equals(p1))))
            .thenReturn(Single.just(new MainResource()));

        resourceService.save(data, testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
    }

    @Test
    void savePlatformNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findByName(sessionManager, "new_r")).thenReturn(Maybe.empty());
        when(regionRepository.findByRegionIdAndPlatformId(sessionManager, reg1.getRegionId(), p1.getPlatformId()))
            .thenReturn(Maybe.just(reg1));
        when(sessionManager.find(Platform.class, p1.getPlatformId())).thenReturn(Maybe.empty());

        resourceService.save(data, testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void saveRegionNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findByName(sessionManager, "new_r")).thenReturn(Maybe.empty());
        when(regionRepository.findByRegionIdAndPlatformId(sessionManager, reg1.getRegionId(), p1.getPlatformId()))
            .thenReturn(Maybe.empty());

        resourceService.save(data, testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void saveAlreadyExists(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findByName(sessionManager, "new_r")).thenReturn(Maybe.just(r1));
        when(regionRepository.findByRegionIdAndPlatformId(sessionManager, reg1.getRegionId(), p1.getPlatformId()))
            .thenReturn(Maybe.just(reg1));

        resourceService.save(data, testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void delete(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(resourceRepository.findByIdAndFetch(sessionManager, r1.getResourceId())).thenReturn(Maybe.just(r1));
        when(sessionManager.remove(r1)).thenReturn(Completable.complete());

        resourceService.delete(r1.getResourceId(),
            testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
    }

    @Test
    void deleteNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(resourceRepository.findByIdAndFetch(sessionManager, r1.getResourceId())).thenReturn(Maybe.empty());

        resourceService.delete(r1.getResourceId(), testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
            testContext.completeNow();
        })));
    }

    @Test
    void updateClusterResource(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(resourceRepository.findClusterByName(sessionManager, cr1.getName())).thenReturn(Maybe.just(cr1));
        try (MockedConstruction<K8sResourceUpdateUtility> ignored = DatabaseUtilMockprovider.mockK8sResourceUpdateUtility(
            sessionManager, cr1, monitoringData)) {
            resourceService.updateClusterResource(cr1.getName(), monitoringData,
                testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
        }
    }

    @Test
    void updateClusterResourceNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(resourceRepository.findClusterByName(sessionManager, cr1.getName())).thenReturn(Maybe.empty());

        resourceService.updateClusterResource(cr1.getName(), monitoringData,
                testContext.failing(throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(MonitoringException.class);
                    assertThat(throwable.getMessage()).isEqualTo("cluster cluster is not registered");
                    testContext.completeNow();
                })));
    }

    @Test
    void encodeResourceListSubResource() {
        JsonArray result = ((ResourceServiceImpl) resourceService).mapResourceListToJsonArray(List.of(sr1));
        JsonObject resultEntry = result.getJsonObject(0);
        assertThat(resultEntry.getLong("main_resource_id")).isEqualTo(cr1.getResourceId());
        assertThat(resultEntry.getJsonObject("region")).isNotNull();
        assertThat(resultEntry.getJsonObject("platform")).isNotNull();
        assertThat(resultEntry.getJsonArray("main_metric_values")).isNotNull();
        assertThat(resultEntry.containsKey("created_at")).isEqualTo(true);
        assertThat(resultEntry.containsKey("updated_at")).isEqualTo(true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"initialized", "uninitialized"})
    void encodeResourceListMainResource(String type) {
        try (MockedStatic<Hibernate> mocked = Mockito.mockStatic(Hibernate.class)) {
            mocked.when(() -> Hibernate.isInitialized(List.of(sr1))).thenReturn(type.equals("initialized"));
            cr1.setSubResources(type.equals("initialized") ? List.of(sr1) : List.of());
            JsonArray result = ((ResourceServiceImpl) resourceService).mapResourceListToJsonArray(List.of(cr1));
            JsonObject resultEntry = result.getJsonObject(0);
            assertThat(resultEntry.getLong("resource_id")).isEqualTo(cr1.getResourceId());
            assertThat(resultEntry.getJsonObject("region")).isNotNull();
            assertThat(resultEntry.getJsonObject("platform")).isNotNull();
            assertThat(resultEntry.getJsonArray("sub_resources").size()).isEqualTo(type.equals("initialized") ? 1 : 0);
            if (type.equals("initialized")) {
                assertThat(resultEntry.getJsonArray("sub_resources").getJsonObject(0)
                    .getJsonObject("main_resource")).isNull();
            }
            assertThat(resultEntry.containsKey("created_at")).isEqualTo(true);
            assertThat(resultEntry.containsKey("updated_at")).isEqualTo(true);
        }
    }
}
