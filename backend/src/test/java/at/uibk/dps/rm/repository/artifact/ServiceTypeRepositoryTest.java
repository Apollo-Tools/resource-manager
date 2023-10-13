package at.uibk.dps.rm.repository.artifact;

import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestServiceProvider;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceTypeRepositoryTest extends DatabaseTest {

    private final ServiceTypeRepository repository = new ServiceTypeRepository();

    @Override
    public void fillDB(VertxTestContext testContext) {
        super.fillDB(testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            ServiceType st1 = TestServiceProvider.createServiceTyp(null, "default");
            ServiceType st2 = TestServiceProvider.createServiceTyp(null, "stFoo");
            return sessionManager.persist(st1)
                .flatMap(res -> sessionManager.persist(st2));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "notype, true, 2",
        "default, true, 3",
        "stFoo, true, 4",
        "notexists, false, -1",
    })
    void findByName(String name, boolean exists, long id, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByName(sessionManager, name))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getArtifactTypeId()).isEqualTo(id);
                    assertThat(result.getName()).isEqualTo(name);
                    testContext.completeNow();
                } else {
                    testContext.failNow("method did not throw exception");
                }
            }), throwable -> {
                assertThat(exists).isEqualTo(false);
                assertThat(throwable.getCause()).isInstanceOf(NoSuchElementException.class);
                testContext.completeNow();
            });
    }
}
