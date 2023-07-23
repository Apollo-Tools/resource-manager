package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ServiceDeploymentService;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link ServiceDeploymentChecker} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class ServiceDeploymentCheckerTest {

    private ServiceDeploymentChecker checker;

    @Mock
    private ServiceDeploymentService service;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        checker = new ServiceDeploymentChecker(service);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void checkReadyForStartup(boolean isReady, VertxTestContext testContext) {
        long deploymentId = 1L, resourceDeploymentId = 2L, accountId = 3L;

        when(service.existsReadyForContainerStartupAndTermination(deploymentId, resourceDeploymentId, accountId))
            .thenReturn(Single.just(isReady));

        checker.checkReadyForStartup(deploymentId, resourceDeploymentId, accountId)
            .blockingSubscribe(() -> testContext.verify(() -> {
                    if (!isReady) {
                        fail("method did not throw exception");
                    }
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> {
                    if (isReady) {
                        fail("method has thrown exception");
                    } else {
                        assertThat(throwable).isInstanceOf(NotFoundException.class);
                    }

                })
            );
        testContext.completeNow();
    }
}
