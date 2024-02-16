package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.ResourceDeploymentStatus;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.deployment.FunctionDeploymentRepository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.testutil.SessionMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Maybe;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ServiceDeploymentServiceImpl} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionDeploymentImplTest {

    private FunctionDeploymentService service;

    @Mock
    FunctionDeploymentRepository repository;

    @Mock
    private SessionManagerProvider smProvider;

    @Mock
    private SessionManager sessionManager;

    private FunctionDeployment fd1;
    private final long resourceDeploymentId = 2L;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        service = new FunctionDeploymentServiceImpl(repository, smProvider);

        Deployment d1 = TestDeploymentProvider.createDeployment(1L);
        fd1 = TestFunctionProvider.createFunctionDeployment(2L, 2L, d1);
    }

    @ParameterizedTest
    @CsvSource({
        "NEW, '', false",
        "DEPLOYED, '', false",
        "NEW, 'triggerUrl', false",
        "DEPLOYED, triggerUrl, true"
    })
    void findOneForDeploymentAndTermination(String status, String directUrl, boolean valid,
            VertxTestContext testContext) {
        DeploymentStatusValue statusValue = DeploymentStatusValue.valueOf(status);
        ResourceDeploymentStatus rds = TestDeploymentProvider
            .createResourceDeploymentStatus(1L, statusValue);
        fd1.setStatus(rds);
        fd1.setDirectTriggerUrl(directUrl);

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        long accountId = 1L;
        when(repository.findByIdAndAccountId(sessionManager, resourceDeploymentId, accountId))
            .thenReturn(Maybe.just(fd1));

        Handler<AsyncResult<JsonObject>> handler;
        if (valid) {
            handler = testContext.succeeding(result -> testContext.verify(() -> {
                assertThat(result.getLong("resource_deployment_id")).isEqualTo(resourceDeploymentId);
                testContext.completeNow();
            }));
        } else {
            handler = testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(BadInputException.class);
                assertThat(throwable.getMessage())
                    .isEqualTo("Function Deployment is not ready for invocation");
                testContext.completeNow();
            }));
        }

        service.findOneForInvocation(resourceDeploymentId, accountId, handler);
    }

    @Test
    void findOneForDeploymentAndTerminationNotFound(VertxTestContext testContext) {
        long accountId = 1L, resourceDeploymentId = 2L;

        SessionMockHelper.mockSingle(smProvider, sessionManager);
        when(repository.findByIdAndAccountId(sessionManager, resourceDeploymentId, accountId))
            .thenReturn(Maybe.empty());

        service.findOneForInvocation(resourceDeploymentId, accountId,
            testContext.failing(throwable -> testContext.verify(() -> {
                assertThat(throwable).isInstanceOf(NotFoundException.class);
                testContext.completeNow();
            })));
    }
}
