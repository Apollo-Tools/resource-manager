package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.entity.dto.service.K8sServiceTypeEnum;
import at.uibk.dps.rm.entity.dto.service.K8sServiceTypeId;
import at.uibk.dps.rm.entity.dto.service.UpdateServiceDTO;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDTOProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import at.uibk.dps.rm.service.database.util.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Implements tests for the {@link ServiceServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceServiceImplTest {

    private ServiceService service;

    @Mock
    private ServiceRepository serviceRepository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private long serviceId, accountId;
    private Account account;
    private Service s1;
    private K8sServiceType k8sStNodePort, k8sStNoService, k8sStLoadBalancer;
    private ServiceType st1;
    private UpdateServiceDTO updateService;
    private EnvVar ev1;
    private VolumeMount vm1;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ServiceServiceImpl(serviceRepository, smProvider);
        serviceId = 1L;
        accountId = 2L;
        account = TestAccountProvider.createAccount(accountId);
        k8sStNodePort = TestServiceProvider.createK8sServiceType(3L, K8sServiceTypeEnum.NODE_PORT.getValue());
        k8sStNoService = TestServiceProvider.createK8sServiceType(2L, K8sServiceTypeEnum.NO_SERVICE.getValue());
        k8sStLoadBalancer = TestServiceProvider.createK8sServiceType(4L, K8sServiceTypeEnum.LOAD_BALANCER.getValue());
        st1 = TestServiceProvider.createServiceTyp(4L, "service-type");
        s1 = TestServiceProvider.createService(serviceId, st1, "apollo-ee", "apollo-ee:latest", k8sStNodePort,
            List.of("80:8888"), account, 1, BigDecimal.valueOf(13.37), 512, List.of(), List.of(), true);
        K8sServiceTypeId k8sServiceTypeId = new K8sServiceTypeId();
        k8sServiceTypeId.setServiceTypeId(k8sStNoService.getServiceTypeId());
        ev1 = TestServiceProvider.createEnvVar(1L);
        vm1 = TestServiceProvider.createVolumeMount(1L);
        updateService = TestDTOProvider.createUpdateServiceDTO(2, List.of(),
            BigDecimal.valueOf(31.5), 205, k8sServiceTypeId, List.of(ev1), List.of(vm1), false);
    }

    @Test
    void findOne(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(serviceRepository.findByIdAndFetch(sessionManager, serviceId))
            .thenReturn(Maybe.just(s1));
        when(sessionManager.fetch(any(List.class)))
            .thenReturn(Single.just(List.of()));

        service.findOne(serviceId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("service_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void findOneNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(serviceRepository.findByIdAndFetch(sessionManager, serviceId)).thenReturn(Maybe.empty());

        service.findOne(serviceId, testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountId(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(serviceRepository.findByIdAndAccountId(sessionManager, serviceId, accountId, true))
            .thenReturn(Maybe.just(s1));

        service.findOneByIdAndAccountId(serviceId, accountId,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("service_id")).isEqualTo(1L);
                testContext.completeNow();
            })));
    }

    @Test
    void findOneByIdAndAccountIdNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockMaybe(smProvider, sessionManager);
        when(serviceRepository.findByIdAndAccountId(sessionManager, serviceId, accountId, true))
            .thenReturn(Maybe.empty());

        service.findOneByIdAndAccountId(serviceId, accountId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void findAll(VertxTestContext testContext) {
        Service s2 = TestServiceProvider.createService(2L);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(serviceRepository.findAllAndFetch(sessionManager)).thenReturn(Single.just(List.of(s1, s2)));

        service.findAll(testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                JsonObject res1 = result.getJsonObject(0);
                assertThat(res1.getLong("service_id")).isEqualTo(1L);
                assertThat(res1.getValue("replicas")).isNull();
                assertThat(res1.getValue("ports")).isNull();
                assertThat(res1.getValue("cpu")).isNull();
                assertThat(res1.getValue("memory")).isNull();
                assertThat(result.getJsonObject(1).getLong("service_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllAccessibleServices(VertxTestContext testContext) {
        Service s2 = TestServiceProvider.createService(2L);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(serviceRepository.findAllAccessibleAndFetch(sessionManager, accountId))
            .thenReturn(Single.just(List.of(s1, s2)));

        service.findAllAccessibleServices(accountId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("service_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("service_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        Service s2 = TestServiceProvider.createService(2L);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(serviceRepository.findAllByAccountId(sessionManager, accountId))
            .thenReturn(Single.just(List.of(s1, s2)));

        service.findAllByAccountId(accountId, testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.getJsonObject(0).getLong("service_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("service_id")).isEqualTo(2L);
                testContext.completeNow();
            })));
    }

    @Test
    void saveToAccount(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(K8sServiceType.class, k8sStNodePort.getServiceTypeId()))
            .thenReturn(Maybe.just(k8sStNodePort));
        when(serviceRepository.findOneByNameTypeAndCreator(sessionManager, s1.getName(), st1.getArtifactTypeId(),
            accountId)).thenReturn(Maybe.empty());
        doReturn(Maybe.just(st1)).when(sessionManager).find(ServiceType.class, st1.getArtifactTypeId());
        doReturn(Maybe.just(account)).when(sessionManager).find(Account.class, accountId);
        when(sessionManager.persist(s1)).thenReturn(Single.just(s1));

        service.saveToAccount(accountId, JsonObject.mapFrom(s1),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("service_id")).isEqualTo(s1.getServiceId());
                assertThat(result.getJsonObject("service_type").getLong("artifact_type_id"))
                    .isEqualTo(4L);
                assertThat(result.getJsonObject("k8s_service_type").getLong("service_type_id")).isEqualTo(3L);
                assertThat(result.getJsonObject("created_by").getLong("account_id")).isEqualTo(2L);
                testContext.completeNow();
            }))
        );
    }

    @Test
    void saveToAccountAccountNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(K8sServiceType.class, k8sStNodePort.getServiceTypeId()))
            .thenReturn(Maybe.just(k8sStNodePort));
        when(serviceRepository.findOneByNameTypeAndCreator(sessionManager, s1.getName(), st1.getArtifactTypeId(),
            accountId)).thenReturn(Maybe.empty());
        doReturn(Maybe.just(st1)).when(sessionManager).find(ServiceType.class, st1.getArtifactTypeId());
        doReturn(Maybe.empty()).when(sessionManager).find(Account.class, accountId);

        service.saveToAccount(accountId, JsonObject.mapFrom(s1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            }))
        );
    }

    @Test
    void saveToAccountServiceTypeNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(K8sServiceType.class, k8sStNodePort.getServiceTypeId()))
            .thenReturn(Maybe.just(k8sStNodePort));
        when(serviceRepository.findOneByNameTypeAndCreator(sessionManager, s1.getName(), st1.getArtifactTypeId(),
            accountId)).thenReturn(Maybe.empty());
        doReturn(Maybe.empty()).when(sessionManager).find(ServiceType.class, st1.getArtifactTypeId());

        service.saveToAccount(accountId, JsonObject.mapFrom(s1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            }))
        );
    }

    @Test
    void saveToAccountAlreadyExists(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(K8sServiceType.class, k8sStNodePort.getServiceTypeId()))
            .thenReturn(Maybe.just(k8sStNodePort));
        when(serviceRepository.findOneByNameTypeAndCreator(sessionManager, s1.getName(), st1.getArtifactTypeId(),
            accountId)).thenReturn(Maybe.just(s1));
        doReturn(Maybe.empty()).when(sessionManager).find(ServiceType.class, st1.getArtifactTypeId());

        service.saveToAccount(accountId, JsonObject.mapFrom(s1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                testContext.completeNow();
            }))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"noServiceWithPorts", "serviceWithNoPorts"})
    void saveToAccountWrongPorts(String type, VertxTestContext testContext) {
        K8sServiceType k8sServiceType = k8sStNodePort;
        List<String> ports = List.of();
        if (type.equals("noServiceWithPorts")) {
            k8sServiceType = k8sStNoService;
            ports = List.of("80:8888");
        }
        s1.setK8sServiceType(k8sServiceType);
        s1.setPorts(ports);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(K8sServiceType.class, k8sServiceType.getServiceTypeId()))
            .thenReturn(Maybe.just(k8sServiceType));
        doReturn(Maybe.empty()).when(sessionManager).find(ServiceType.class, st1.getArtifactTypeId());

        service.saveToAccount(accountId, JsonObject.mapFrom(s1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage()).isEqualTo("invalid ports for service selection");
                testContext.completeNow();
            }))
        );
    }

    @Test
    void saveToAccountK8sServiceTypeNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(sessionManager.find(K8sServiceType.class, k8sStNodePort.getServiceTypeId()))
            .thenReturn(Maybe.empty());
        doReturn(Maybe.empty()).when(sessionManager).find(ServiceType.class, st1.getArtifactTypeId());

        service.saveToAccount(accountId, JsonObject.mapFrom(s1),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            }))
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"newK8sSt", "newPorts", "bothNew"})
    void updateOwned(String type, VertxTestContext testContext) {
        K8sServiceType k8sServiceType;
        List<String> ports;
        if (type.equals("newK8sSt")) {
            ports = s1.getPorts();
            k8sServiceType = k8sStLoadBalancer;
            K8sServiceTypeId k8sServiceTypeId = new K8sServiceTypeId();
            k8sServiceTypeId.setServiceTypeId(k8sStLoadBalancer.getServiceTypeId());
            updateService.setK8sServiceType(k8sServiceTypeId);
            updateService.setPorts(null);
        } else if (type.equals("newPorts")) {
            k8sServiceType = s1.getK8sServiceType();
            ports = List.of("90:9999");
            updateService.setPorts(ports);
            updateService.setK8sServiceType(null);
        } else {
            k8sServiceType = k8sStNoService;
            ports = updateService.getPorts();
        }
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(serviceRepository.findByIdAndAccountId(sessionManager, serviceId, accountId, false))
            .thenReturn(Maybe.just(s1));
        when(sessionManager.find(K8sServiceType.class, k8sServiceType.getServiceTypeId()))
            .thenReturn(Maybe.just(k8sServiceType));
        when(sessionManager.fetch(any(List.class))).thenReturn(Single.just(List.of()));
        service.updateOwned(s1.getServiceId(), accountId, JsonObject.mapFrom(updateService),
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(s1.getReplicas()).isEqualTo(2);
                assertThat(s1.getPorts()).isEqualTo(ports);
                assertThat(s1.getCpu()).isEqualTo(BigDecimal.valueOf(31.5));
                assertThat(s1.getMemory()).isEqualTo(205);
                assertThat(s1.getK8sServiceType().getName())
                    .isEqualTo(k8sServiceType.getName());
                assertThat(s1.getEnvVars()).isEqualTo(List.of(ev1));
                assertThat(s1.getVolumeMounts()).isEqualTo(List.of(vm1));
                assertThat(s1.getIsPublic()).isEqualTo(false);
                testContext.completeNow();
            })));
    }

    @Test
    void updateOwnedInvalidPorts(VertxTestContext testContext) {
        updateService.setPorts(List.of("90:9999"));
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(serviceRepository.findByIdAndAccountId(sessionManager, serviceId, accountId, false))
            .thenReturn(Maybe.just(s1));
        when(sessionManager.find(K8sServiceType.class, k8sStNoService.getServiceTypeId())).thenReturn(Maybe.just(k8sStNoService));
        service.updateOwned(s1.getServiceId(), accountId, JsonObject.mapFrom(updateService),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage()).isEqualTo("invalid ports for service selection");
                testContext.completeNow();
            })));
    }

    @Test
    void updateOwnedK8sServiceTypeNotFound(VertxTestContext testContext) {
        updateService.setPorts(List.of());
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(serviceRepository.findByIdAndAccountId(sessionManager, serviceId, accountId, false))
            .thenReturn(Maybe.just(s1));
        when(sessionManager.find(K8sServiceType.class, k8sStNoService.getServiceTypeId())).thenReturn(Maybe.empty());
        service.updateOwned(s1.getServiceId(), accountId, JsonObject.mapFrom(updateService),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void updateOwnedServiceNotFound(VertxTestContext testContext) {
        updateService.setPorts(List.of());
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(serviceRepository.findByIdAndAccountId(sessionManager, serviceId, accountId, false))
            .thenReturn(Maybe.empty());
        service.updateOwned(s1.getServiceId(), accountId, JsonObject.mapFrom(updateService),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }

    @Test
    void deleteFromAccount(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(serviceRepository.findByIdAndAccountId(sessionManager, s1.getServiceId(), accountId, false))
            .thenReturn(Maybe.just(s1));
        when(sessionManager.remove(s1)).thenReturn(Completable.complete());
        service.deleteFromAccount(accountId, s1.getServiceId(),
            testContext.succeeding(result -> testContext.verify(testContext::completeNow))
        );
    }

    @Test
    void deleteFromAccountNotFound(VertxTestContext testContext) {
        SessionMockHelper.mockCompletable(smProvider, sessionManager);
        when(serviceRepository.findByIdAndAccountId(sessionManager, s1.getServiceId(), accountId, false))
            .thenReturn(Maybe.empty());
        service.deleteFromAccount(accountId, s1.getServiceId(),
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }
}
