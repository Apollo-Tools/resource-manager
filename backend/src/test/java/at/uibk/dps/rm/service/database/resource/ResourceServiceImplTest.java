package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.resource.FindAllFunctionDeploymentScrapeTargetsDTO;
import at.uibk.dps.rm.entity.dto.resource.FindAllOpenFaaSScrapeTargetsDTO;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sMonitoringData;
import at.uibk.dps.rm.entity.monitoring.kubernetes.K8sNode;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
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
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
    private RegionRepository regionRepository;

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
    private Deployment deployment;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        resourceService = new ResourceServiceImpl(resourceRepository, regionRepository, smProvider);
        data = new JsonObject("{\"name\": \"new_r\", \"region\": {\"region_id\": 1}, \"platform\": " +
            "{\"platform_id\":  2}, \"is_lockable\": true}");
        reg1 = TestResourceProviderProvider.createRegion(1L, "us-east");
        p1 = TestPlatformProvider.createPlatformFaas(2L, "lambda");
        r1 = TestResourceProvider.createResource(1L);
        r2 = TestResourceProvider.createResource(2L);
        cr1 = TestResourceProvider.createClusterWithoutNodes(3L, "cluster");
        sr1 = TestResourceProvider.createSubResource(4L, "subresource1", cr1);
        sr2 = TestResourceProvider.createSubResource(5L, "subresource2", cr1);
        K8sNode k8sn1 = TestK8sProvider.createK8sNode("n1", 10.0, 8.75,
            1000, 500, 10000, 5000);
        V1Namespace namespace = TestK8sProvider.createNamespace("default");
        monitoringData = new K8sMonitoringData("cluster", "http://clusterurl:9999", 1L,
            List.of(k8sn1), List.of(namespace), true, 0.15);
        deployment = TestDeploymentProvider.createDeployment(1L);
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
        r1.setLockedByDeployment(deployment);
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findAllMainResourcesAndFetch(sessionManager)).thenReturn(Single.just(List.of(r1, r2)));

        resourceService.findAll(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(0).getBoolean("is_locked")).isEqualTo(true);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getBoolean("is_locked")).isEqualTo(false);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByNonMonitoredSLOs(VertxTestContext testContext) {
        ServiceLevelObjective slo1 = new ServiceLevelObjective("instance-type", ExpressionType.EQ,
            TestDTOProvider.createSLOValueList("t2.micro"));
        SLORequest sloRequest = TestDTOProvider.createSLORequest(List.of(slo1));

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        try (MockedConstruction<SLOUtility> ignored = DatabaseUtilMockprovider
                .mockSLOUtilityFindAndFilterResources(sessionManager, List.of(r1, r2))) {
            resourceService.findAllByNonMonitoredSLOs(JsonObject.mapFrom(sloRequest),
                testContext.succeeding(result -> testContext.verify(() -> {
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
    void findAllByDeployment(VertxTestContext testContext) {
        r1.setLockedByDeployment(deployment);
        r2.setLockedByDeployment(deployment);
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findAllLockedByDeploymentId(sessionManager, deployment.getDeploymentId()))
            .thenReturn(Single.just(List.of(r1, r2)));

        resourceService.findAllLockedByDeployment(deployment.getDeploymentId(),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(0).getBoolean("is_locked")).isEqualTo(true);
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getBoolean("is_locked")).isEqualTo(true);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByPlatform(VertxTestContext testContext) {
        r1.getMain().setPlatform(p1);
        r2.getMain().setPlatform(p1);
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findAllMainResourcesByPlatform(sessionManager, p1.getPlatform()))
            .thenReturn(Single.just(List.of(r1, r2)));

        resourceService.findAllByPlatform(p1.getPlatform(),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("resource_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(0).getJsonObject("platform").getLong("platform_id"))
                    .isEqualTo(p1.getPlatformId());
                assertThat(result.getJsonObject(1).getLong("resource_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(1).getJsonObject("platform").getLong("platform_id"))
                    .isEqualTo(p1.getPlatformId());
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
    void findAllScrapeTargets(VertxTestContext testContext) {
        Resource r3 = TestResourceProvider.createResource(3L);
        FindAllFunctionDeploymentScrapeTargetsDTO st1 =
            TestDBDTOProvider.createFindFunctionDeploymentScrapeTarget(r1.getResourceId(), "http://r1.com", 9101);
        FindAllFunctionDeploymentScrapeTargetsDTO st2 =
            TestDBDTOProvider.createFindFunctionDeploymentScrapeTarget(r3.getResourceId(), "http://r3.com", 9103);
        FindAllOpenFaaSScrapeTargetsDTO st3 =
            TestDBDTOProvider.createFindOpenFaaSScrapeTarget(r2.getResourceId(), "http://r2.com",
                BigDecimal.valueOf(9102));
        FindAllOpenFaaSScrapeTargetsDTO stNoBaseUrl =
            TestDBDTOProvider.createFindOpenFaaSScrapeTarget(5L, null, BigDecimal.valueOf(9102));
        FindAllOpenFaaSScrapeTargetsDTO stNoMetricsPort =
            TestDBDTOProvider.createFindOpenFaaSScrapeTarget(6L, "http://r6.com", null);
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findAllFunctionDeploymentTargets(sessionManager))
            .thenReturn(Single.just(Set.of(st1, st2)));
        when(resourceRepository.findAllOpenFaaSTargets(sessionManager))
            .thenReturn(Single.just(List.of(st3, stNoBaseUrl, stNoMetricsPort)));

        resourceService.findAllScrapeTargets(
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                JsonObject res1 = result.stream()
                    .map(resource -> (JsonObject) resource)
                    .filter(resource -> resource.getJsonObject("labels").getString("resource").equals("1"))
                    .findFirst()
                    .orElse(new JsonObject());
                JsonObject res2 = result.stream()
                    .map(resource -> (JsonObject) resource)
                    .filter(resource -> resource.getJsonObject("labels").getString("resource").equals("2"))
                    .findFirst()
                    .orElse(new JsonObject());
                JsonObject res3 = result.stream()
                    .map(resource -> (JsonObject) resource)
                    .filter(resource -> resource.getJsonObject("labels").getString("resource").equals("3"))
                    .findFirst()
                    .orElse(new JsonObject());
                assertThat(res1.getJsonObject("labels").getString("deployment"))
                    .isEqualTo("2");
                assertThat(res1.getJsonObject("labels").getString("resource_deployment"))
                    .isEqualTo("1");
                assertThat(res1.getString("targets")).isEqualTo("[http://r1.com:9101/metrics]");
                assertThat(res2.getJsonObject("labels").containsKey("deployment")).isEqualTo(false);
                assertThat(res2.getJsonObject("labels").containsKey("resource_deployment"))
                    .isEqualTo(false);
                assertThat(res2.getString("targets")).isEqualTo("[http://r2.com:9102/metrics]");
                assertThat(res3.getJsonObject("labels").getString("deployment")).isEqualTo("2");
                assertThat(res3.getJsonObject("labels").getString("resource_deployment"))
                    .isEqualTo("1");
                assertThat(res3.getString("targets")).isEqualTo("[http://r3.com:9103/metrics]");
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

    @ParameterizedTest
    @CsvSource({
        "true, true",
        "true, false",
        "false, true",
        "false, false"
    })
    void update(boolean isLockableCurrent, boolean isLockableNew, VertxTestContext testContext) {
        r1.setIsLockable(isLockableCurrent);
        r1.setLockedByDeployment(deployment);
        JsonObject data = new JsonObject("{\"is_lockable\": " + isLockableNew + "}");

        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(sessionManager.find(Resource.class, r1.getResourceId()))
            .thenReturn(Maybe.just(r1));

        resourceService.update(r1.getResourceId(), data, testContext.succeeding(result -> testContext.verify(() -> {
            assertThat(r1.getIsLockable()).isEqualTo(isLockableNew);
            assertThat(r1.getLockedByDeployment() == null).isEqualTo(!isLockableNew);
            testContext.completeNow();
        })));
    }

    @Test
    void updateNotFound(VertxTestContext testContext) {
        JsonObject data = new JsonObject("{\"is_lockable\": true}");

        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(sessionManager.find(Resource.class, r1.getResourceId())).thenReturn(Maybe.empty());

        resourceService.update(r1.getResourceId(), data, testContext.failing(throwable -> testContext.verify(() -> {
            assertThat(throwable).isInstanceOf(NotFoundException.class);
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
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findClusterByName(sessionManager, cr1.getName())).thenReturn(Maybe.just(cr1));

        try (MockedConstruction<K8sResourceUpdateUtility> ignored = DatabaseUtilMockprovider.mockK8sResourceUpdateUtility(
                sessionManager, cr1, monitoringData)) {
            resourceService.updateClusterResource(cr1.getName(), monitoringData,
                testContext.succeeding(result -> testContext.verify(testContext::completeNow)));
        }
    }

    @Test
    void updateClusterResourceNotUp(VertxTestContext testContext) {
        monitoringData.setIsUp(false);
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findClusterByName(sessionManager, cr1.getName())).thenReturn(Maybe.just(cr1));

        resourceService.updateClusterResource(cr1.getName(), monitoringData, testContext
            .succeeding(result -> testContext.verify(testContext::completeNow)));
    }

    @Test
    void updateClusterResourceNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(resourceRepository.findClusterByName(sessionManager, cr1.getName())).thenReturn(Maybe.empty());

        resourceService.updateClusterResource(cr1.getName(), monitoringData,
                testContext.failing(throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    assertThat(throwable.getMessage()).isEqualTo("cluster cluster is not registered");
                    testContext.completeNow();
                })));
    }

    @Test
    void unlockLockedResourcesByDeploymentId(VertxTestContext testContext) {
        long deploymentId = 1L;

        SessionMockHelper.mockCompletable(smProvider, sessionManager);

        try(MockedConstruction<LockedResourcesUtility> ignore = DatabaseUtilMockprovider
            .mockLockUtilityUnlockResources(sessionManager, deploymentId)) {
            resourceService.unlockLockedResourcesByDeploymentId(deploymentId,
                testContext.succeeding(result -> testContext.completeNow()));
        }
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
