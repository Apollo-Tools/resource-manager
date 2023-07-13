package at.uibk.dps.rm.handler.log;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.handler.deployment.DeploymentChecker;
import at.uibk.dps.rm.testutil.RoutingContextMockHelper;
import at.uibk.dps.rm.testutil.objectprovider.TestAccountProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestLogProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link DeploymentLogHandler} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class DeploymentLogHandlerTest {

    private DeploymentLogHandler handler;

    @Mock
    private DeploymentLogChecker deploymentLogChecker;

    @Mock
    private LogChecker logChecker;

    @Mock
    private DeploymentChecker deploymentChecker;

    @Mock
    private RoutingContext rc;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        handler = new DeploymentLogHandler(deploymentLogChecker, logChecker, deploymentChecker);
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid", "empty"})
    void getAll(String testCase, VertxTestContext testContext) {
        Account account = TestAccountProvider.createAccount(22L);
        Deployment deployment = TestDeploymentProvider.createDeployment(11L);
        DeploymentLog rl1 = TestLogProvider.createDeploymentLog(1L, deployment);
        DeploymentLog rl2 = TestLogProvider.createDeploymentLog(2L, deployment);
        JsonArray deploymentLogs = new JsonArray(List.of(JsonObject.mapFrom(rl1), JsonObject.mapFrom(rl2)));
        if (testCase.equals("empty")) {
            deploymentLogs = new JsonArray();
        }

        RoutingContextMockHelper.mockUserPrincipal(rc, account);
        when(rc.pathParam("id")).thenReturn(String.valueOf(deployment.getDeploymentId()));
        when(deploymentChecker.checkFindOne(11L, 22L)).thenReturn(Single.just(JsonObject.mapFrom(deployment)));
        when(logChecker.checkFindAllByDeploymentId(11L, 22L))
            .thenReturn(Single.just(deploymentLogs));

        handler.getAll(rc)
            .subscribe(result -> testContext.verify(() -> {
                    if (testCase.equals("valid")) {
                        assertThat(result.size()).isEqualTo(2);
                        assertThat(result.getJsonObject(0).getLong("deployment_log_id")).isEqualTo(1L);
                        assertThat(result.getJsonObject(1).getLong("deployment_log_id")).isEqualTo(2L);
                    } else {
                        assertThat(result.size()).isEqualTo(0);
                    }
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
    }
}
