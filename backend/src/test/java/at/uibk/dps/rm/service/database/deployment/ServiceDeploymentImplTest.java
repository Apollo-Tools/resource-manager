package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.ResourceDeploymentStatus;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new ServiceDeploymentServiceImpl(repository, smProvider);
    }

    @ParameterizedTest
    @CsvSource({
        "NEW, false",
        "DEPLOYED, true"
    })
    void findOneForDeploymentAndTermination(String status, boolean valid,
            VertxTestContext testContext) {
        long accountId = 1L, resourceDeploymentId = 2L;
        DeploymentStatusValue statusValue = DeploymentStatusValue.valueOf(status);
        Deployment d1 = TestDeploymentProvider.createDeployment(1L);
        ResourceDeploymentStatus rds = TestDeploymentProvider
            .createResourceDeploymentStatus(1L, statusValue);
        ServiceDeployment sd = TestServiceProvider.createServiceDeployment(2L, 2L, d1);
        sd.setStatus(rds);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repository.findByIdAndAccountId(sessionManager, resourceDeploymentId, accountId))
            .thenReturn(Maybe.just(sd));

        Handler<AsyncResult<JsonObject>> handler;
        if (valid) {
            when(sessionManager.fetch(any())).thenReturn(Single.just(List.of()));
            handler = testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("resource_deployment_id")).isEqualTo(resourceDeploymentId);
                testContext.completeNow();
            }));
        } else {
            handler = testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage())
                    .isEqualTo("Service Deployment is not ready for startup/termination");
                testContext.completeNow();
            }));
        }

        service.findOneForDeploymentAndTermination(resourceDeploymentId, accountId, handler);
    }

    @Test
    void findOneForDeploymentAndTerminationNotFound(VertxTestContext testContext) {
        long accountId = 1L, resourceDeploymentId = 2L;

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repository.findByIdAndAccountId(sessionManager, resourceDeploymentId, accountId))
            .thenReturn(Maybe.empty());

        service.findOneForDeploymentAndTermination(resourceDeploymentId, accountId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }
}
