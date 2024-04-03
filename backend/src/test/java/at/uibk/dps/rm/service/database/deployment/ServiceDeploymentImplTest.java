package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.ResourceDeploymentStatus;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.junit5.VertxExtension;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ServiceDeploymentServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceDeploymentImplTest {

    private ServiceDeploymentService service;

    @Mock
    ServiceDeploymentRepository repository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private final long accountId = 1L;
    private final long deploymentId = 2L;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ServiceDeploymentServiceImpl(repository, smProvider);
    }

    private static Stream<Arguments> provideFindAllForServiceOperation() {
        return Stream.of(
            Arguments.of(List.of(1L, 2L, 3L), List.of("DEPLOYED", "DEPLOYED", "DEPLOYED"), true),
            Arguments.of(List.of(1L, 2L, 3L), List.of("DEPLOYED", "NEW", "DEPLOYED"), false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindAllForServiceOperation")
    void findAllForServiceOperation(List<Long> resourceDeploymentIds, List<String> statusList, boolean valid,
            VertxTestContext testContext) {
        Deployment d1 = TestDeploymentProvider.createDeployment(deploymentId);
        List<ServiceDeployment> serviceDeployments = new ArrayList<>();
        for (int i = 0; i < resourceDeploymentIds.size(); i++) {
            DeploymentStatusValue statusValue = DeploymentStatusValue.valueOf(statusList.get(i));
            ResourceDeploymentStatus rds = TestDeploymentProvider
                .createResourceDeploymentStatus(resourceDeploymentIds.get(i), statusValue);
            ServiceDeployment sd = TestServiceProvider.createServiceDeployment(resourceDeploymentIds.get(i), 2L, d1);
            sd.setStatus(rds);
            serviceDeployments.add(sd);
        }

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repository
            .findAllByIdsAccountIdAndDeploymentId(sessionManager, resourceDeploymentIds, accountId, deploymentId))
            .thenReturn(Single.just(serviceDeployments));
        when(sessionManager.fetch(any())).thenReturn(Single.just(List.of()));

        Handler<AsyncResult<JsonArray>> handler;
        if (valid) {
            handler = testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.getJsonObject(0).getLong("resource_deployment_id")).isEqualTo(1L);
                assertThat(result.getJsonObject(1).getLong("resource_deployment_id")).isEqualTo(2L);
                assertThat(result.getJsonObject(2).getLong("resource_deployment_id")).isEqualTo(3L);
                testContext.completeNow();
            }));
        } else {
            handler = testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage())
                    .isEqualTo("Service Deployment is not ready for startup/shutdown");
                testContext.completeNow();
            }));
        }

        service.findAllForServiceOperation(resourceDeploymentIds, accountId, deploymentId, handler);
    }

    @Test
    void findAllForServiceOperationEmpty(VertxTestContext testContext) {
        long resourceDeploymentId = 2L;

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repository.findAllByIdsAccountIdAndDeploymentId(sessionManager, List.of(resourceDeploymentId),
            accountId, deploymentId))
            .thenReturn(Single.just(List.of()));

        service.findAllForServiceOperation(List.of(resourceDeploymentId), accountId, deploymentId,
            testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.isEmpty()).isEqualTo(true);
                testContext.completeNow();
            })));
    }
}
