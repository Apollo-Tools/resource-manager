package at.uibk.dps.rm.repository.artifact;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.*;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.core.Vertx;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;

public class FunctionTypeRepositoryTest extends DatabaseTest {

    private final FunctionTypeRepository repository = new FunctionTypeRepository();

    @Override
    public void fillDB(Vertx vertx, VertxTestContext testContext) {
        super.fillDB(vertx, testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            FunctionType ft1 = TestFunctionProvider.createFunctionType(null, "default");
            FunctionType ft2 = TestFunctionProvider.createFunctionType(null, "ftFoo");
            return sessionManager.persist(ft1)
                .flatMap(res -> sessionManager.persist(ft2));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "notype, true, 1",
        "default, true, 3",
        "ftFoo, true, 4",
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
