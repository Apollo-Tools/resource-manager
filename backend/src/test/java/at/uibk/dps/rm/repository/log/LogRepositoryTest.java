package at.uibk.dps.rm.repository.log;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestDeploymentProvider;
import at.uibk.dps.rm.testutil.objectprovider.TestLogProvider;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

public class LogRepositoryTest extends DatabaseTest {

    private final LogRepository repository = new LogRepository();

    @Override
    public void fillDB(VertxTestContext testContext) {
        super.fillDB(testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Deployment d1 = TestDeploymentProvider.createDeployment(null, true, accountAdmin);
            Deployment d2 = TestDeploymentProvider.createDeployment(null, true, accountDefault);
            Log l1 = TestLogProvider.createLog(null);
            Log l2 = TestLogProvider.createLog(null);
            Log l3 = TestLogProvider.createLog(null);
            Log l4 = TestLogProvider.createLog(null);
            DeploymentLog dl1 = TestLogProvider.createDeploymentLog(null, d1, l1);
            DeploymentLog dl2 = TestLogProvider.createDeploymentLog(null, d1, l2);
            DeploymentLog dl3 = TestLogProvider.createDeploymentLog(null, d2, l3);
            DeploymentLog dl4 = TestLogProvider.createDeploymentLog(null, d2, l4);
            return sessionManager.persist(d1)
                .flatMap(res -> sessionManager.persist(d2))
                .flatMap(res -> sessionManager.persist(l1))
                .flatMap(res -> sessionManager.persist(l2))
                .flatMap(res -> sessionManager.persist(l3))
                .flatMap(res -> sessionManager.persist(l4))
                .flatMap(res -> sessionManager.persist(dl1))
                .flatMap(res -> sessionManager.persist(dl2))
                .flatMap(res -> sessionManager.persist(dl3))
                .flatMap(res -> sessionManager.persist(dl4));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, 1, 2",
        "2, 2, 3, 4",
        "1, 2, -1, -1",
    })
    void findAllByDeploymentIdAndAccountId(long deploymentId, long accountId, long id1, long id2,
            VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByDeploymentIdAndAccountId(sessionManager,
                deploymentId, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (id1 == -1) {
                    assertThat(result.size()).isEqualTo(0);
                } else {
                    assertThat(result.size()).isEqualTo(2);
                    assertThat(result.get(0).getLogId()).isEqualTo(id1);
                    assertThat(result.get(1).getLogId()).isEqualTo(id2);
                }
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
