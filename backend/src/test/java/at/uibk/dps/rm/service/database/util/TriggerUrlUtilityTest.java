package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.deployment.output.TFOutputValueFaas;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.testutil.mockprovider.DeploymentRepositoryProviderMock;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.hibernate.reactive.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link TriggerUrlUtility} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class TriggerUrlUtilityTest {

    private TriggerUrlUtility utility;

    private final DeploymentRepositoryProviderMock repositoryMock = new DeploymentRepositoryProviderMock();

    @Mock
    private Stage.Session session;

    @Mock
    private SessionManager sessionManager;



    @BeforeEach
    void initTest() {
        repositoryMock.mockRepositories();
        sessionManager = new SessionManager(session);
        utility = new TriggerUrlUtility(repositoryMock.getRepositoryProvider());
    }

    @Test
    void setTriggerUrlsForFunction(VertxTestContext testContext) {
        DeploymentOutput output = TestDeploymentProvider.createDeploymentOutput("python38");
        DeployResourcesDTO deployResourcesDTO = TestRequestProvider.createDeployRequest();

        when(repositoryMock.getFunctionDeploymentRepository()
            .updateTriggerUrls(eq(sessionManager), eq(1L), eq("/function-deployments/1/invoke"),
                argThat((TFOutputValueFaas tfOutputValueFaas) -> tfOutputValueFaas.getFullUrl().equals("http://host:8080/foo1"))))
            .thenReturn(Completable.complete());
        when(repositoryMock.getFunctionDeploymentRepository()
            .updateTriggerUrls(eq(sessionManager), eq(4L), eq("/function-deployments/4/invoke"),
                argThat((TFOutputValueFaas tfOutputValueFaas) -> tfOutputValueFaas.getFullUrl().equals("http://host:8080/foo2"))))
            .thenReturn(Completable.complete());

        utility.setTriggerUrlsForFunctions(sessionManager, output, deployResourcesDTO)
            .blockingSubscribe(testContext::completeNow,
                throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void setTriggerUrlsForFunctionNotFound(VertxTestContext testContext) {
        DeploymentOutput output = TestDeploymentProvider.createDeploymentOutput("python38");
        DeployResourcesDTO deployResourcesDTO = TestRequestProvider.createDeployRequest();
        deployResourcesDTO.setFunctionDeployments(List.of());

        utility.setTriggerUrlsForFunctions(sessionManager, output, deployResourcesDTO)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    assertThat(throwable.getMessage()).isEqualTo("trigger url could not be set up for " +
                        "function deployment");
                    testContext.completeNow();
                })
            );
    }

    @Test
    void setTriggerUrlsForFunctionWrongRuntime(VertxTestContext testContext) {
        DeploymentOutput output = TestDeploymentProvider.createDeploymentOutput("java11");
        DeployResourcesDTO deployResourcesDTO = TestRequestProvider.createDeployRequest();

        utility.setTriggerUrlsForFunctions(sessionManager, output, deployResourcesDTO)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    assertThat(throwable.getMessage()).isEqualTo("trigger url could not be set up for " +
                        "function deployment");
                    testContext.completeNow();
                })
            );
    }

    @Test
    void setTriggerUrlForServices(VertxTestContext testContext) {
        DeployResourcesDTO deployResourcesDTO = TestRequestProvider.createDeployRequest();

        when(repositoryMock.getResourceDeploymentRepository()
            .updateRmTriggerUrl(sessionManager, 4L, "/service-deployments/startup"))
            .thenReturn(Completable.complete());
        when(repositoryMock.getResourceDeploymentRepository()
            .updateRmTriggerUrl(sessionManager, 5L, "/service-deployments/startup"))
            .thenReturn(Completable.complete());
        when(repositoryMock.getResourceDeploymentRepository()
            .updateRmTriggerUrl(sessionManager, 6L, "/service-deployments/startup"))
            .thenReturn(Completable.complete());

        utility.setTriggerUrlForServices(sessionManager, deployResourcesDTO)
            .blockingSubscribe(() -> testContext.verify(testContext::completeNow),
                throwable -> testContext.failNow("method has thrown exception"));
    }
}
