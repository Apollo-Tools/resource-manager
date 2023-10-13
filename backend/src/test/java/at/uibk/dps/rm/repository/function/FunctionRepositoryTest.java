package at.uibk.dps.rm.repository.function;

import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.testutil.DatabaseTest;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.NoSuchElementException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FunctionRepositoryTest extends DatabaseTest {

    private final FunctionRepository repository = new FunctionRepository();

    @Override
    public void fillDB(VertxTestContext testContext) {
        super.fillDB(testContext);

        smProvider.withTransactionSingle(sessionManager -> {
            Runtime rtPython = TestFunctionProvider.createRuntime(1L);
            Runtime rtJava = TestFunctionProvider.createRuntime(2L);
            FunctionType ft1 = TestFunctionProvider.createFunctionType(1L, "notype");
            FunctionType ft2 = TestFunctionProvider.createFunctionType(null, "ftnew");
            Function f1 = TestFunctionProvider.createFunction(null, ft1, "foo1",
                "def main():\n  print()\n", rtPython, false, 300, 1024, true, accountAdmin);
            Function f2 = TestFunctionProvider.createFunction(null, ft1, "foo2",
                "file123455", rtJava, true, 300, 1024, true, accountAdmin);
            Function f3 = TestFunctionProvider.createFunction(null, ft1, "foo3",
                "file019442", rtPython, true, 300, 1024, false, accountDefault);
            Function f4 = TestFunctionProvider.createFunction(null, ft2, "foo4",
                "file123455", rtJava, true, 300, 1024, true, accountDefault);
            return sessionManager.persist(ft2)
                .flatMap(res -> sessionManager.persist(f1))
                .flatMap(res -> sessionManager.persist(f2))
                .flatMap(res -> sessionManager.persist(f3))
                .flatMap(res -> sessionManager.persist(f4));
        }).blockingSubscribe(res -> {}, testContext::failNow);
    }

    @ParameterizedTest
    @CsvSource({
        "1, true, foo1, python3.8, notype",
        "2, true, foo2, java11, notype",
        "3, true, foo3, python3.8, notype",
        "4, true, foo4, java11, ftnew",
        "5, false, foo5, java11, ftnew",
    })
    void findByIdAndFetch(long id, boolean exists, String name, String runtime,
            String functionType, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository.findByIdAndFetch(sessionManager, id))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getFunctionId()).isEqualTo(id);
                    assertThat(result.getName()).isEqualTo(name);
                    assertThat(result.getRuntime().getName()).isEqualTo(runtime);
                    assertThat(result.getFunctionType().getName()).isEqualTo(functionType);
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

    @ParameterizedTest
    @CsvSource({
        "1, 1, false, true, foo1, python3.8, notype, user1",
        "1, 1, true, true, foo1, python3.8, notype, user1",
        "1, 2, true, true, foo1, python3.8, notype, user1",
        "1, 2, false, false, foo1, python3.8, notype, user1",
        "3, 1, true, false, foo3, python3.8, notype, user2",
        "3, 1, false, false, foo3, python3.8, notype, user2",
        "3, 2, true, true, foo3, python3.8, notype, user2",
        "3, 2, false, true, foo3, python3.8, notype, user2"
    })
    void findByIdAndAccountId(long id, long accountId, boolean includePublic, boolean exists, String name,
            String runtime, String functionType, String username, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findByIdAndAccountId(sessionManager, id, accountId, includePublic))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getFunctionId()).isEqualTo(id);
                    assertThat(result.getName()).isEqualTo(name);
                    assertThat(result.getRuntime().getName()).isEqualTo(runtime);
                    assertThat(result.getFunctionType().getName()).isEqualTo(functionType);
                    assertThat(result.getCreatedBy().getUsername()).isEqualTo(username);
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

    @ParameterizedTest
    @CsvSource({
        "foo1, 1, 1, 1, true, 1",
        "foo2, 1, 1, 1, false, 1",
        "foo1, 2, 1, 1, false, 1",
        "foo1, 1, 2, 1, false, 1",
        "foo1, 1, 1, 2, false, 1",
    })
    void findOneByNameTypeRuntimeAndCreator(String name, long typeId, long runtimeId, long accountId, boolean exists,
            long id, VertxTestContext testContext) {
        smProvider.withTransactionMaybe(sessionManager -> repository
                .findOneByNameTypeRuntimeAndCreator(sessionManager, name, typeId, runtimeId, accountId))
            .subscribe(result -> testContext.verify(() -> {
                if (exists) {
                    assertThat(result.getFunctionId()).isEqualTo(id);
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

    @Test
    void findAllAndFetch(VertxTestContext testContext) {
        smProvider.withTransactionSingle(repository::findAllAndFetch)
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(4);
                assertThat(result.get(0).getFunctionId()).isEqualTo(1L);
                assertThat(result.get(0).getRuntime().getName()).isEqualTo("python3.8");
                assertThat(result.get(0).getFunctionType().getName()).isEqualTo("notype");
                assertThat(result.get(1).getFunctionId()).isEqualTo(2L);
                assertThat(result.get(1).getRuntime().getName()).isEqualTo("java11");
                assertThat(result.get(1).getFunctionType().getName()).isEqualTo("notype");
                assertThat(result.get(2).getFunctionId()).isEqualTo(3L);
                assertThat(result.get(2).getRuntime().getName()).isEqualTo("python3.8");
                assertThat(result.get(2).getFunctionType().getName()).isEqualTo("notype");
                assertThat(result.get(3).getFunctionId()).isEqualTo(4L);
                assertThat(result.get(3).getRuntime().getName()).isEqualTo("java11");
                assertThat(result.get(3).getFunctionType().getName()).isEqualTo("ftnew");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"empty", "nonempty"})
    void findAllByIds(String type, VertxTestContext testContext) {
        Set<Long> functionIds = type.equals("empty") ? Set.of() : Set.of(1L, 3L, 43L);

        smProvider.withTransactionSingle(sessionManager -> repository.findAllByIds(sessionManager, functionIds))
            .subscribe(result -> testContext.verify(() -> {
                if (type.equals("empty")) {
                    assertThat(result.size()).isEqualTo(0);
                } else {
                    assertThat(result.get(0).getFunctionId()).isEqualTo(1L);
                    assertThat(result.get(1).getFunctionId()).isEqualTo(3L);
                }
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void findAllAccessibleAndFetch(VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllAccessibleAndFetch(sessionManager, 1L))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(3);
                assertThat(result.get(0).getFunctionId()).isEqualTo(1L);
                assertThat(result.get(0).getRuntime().getName()).isEqualTo("python3.8");
                assertThat(result.get(0).getFunctionType().getName()).isEqualTo("notype");
                assertThat(result.get(1).getFunctionId()).isEqualTo(2L);
                assertThat(result.get(1).getRuntime().getName()).isEqualTo("java11");
                assertThat(result.get(1).getFunctionType().getName()).isEqualTo("notype");
                assertThat(result.get(2).getFunctionId()).isEqualTo(4L);
                assertThat(result.get(2).getRuntime().getName()).isEqualTo("java11");
                assertThat(result.get(2).getFunctionType().getName()).isEqualTo("ftnew");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }

    @Test
    void findAllByAccountId(VertxTestContext testContext) {
        smProvider.withTransactionSingle(sessionManager -> repository.findAllByAccountId(sessionManager, 1L))
            .subscribe(result -> testContext.verify(() -> {
                assertThat(result.size()).isEqualTo(2);
                assertThat(result.get(0).getFunctionId()).isEqualTo(1L);
                assertThat(result.get(0).getRuntime().getName()).isEqualTo("python3.8");
                assertThat(result.get(0).getFunctionType().getName()).isEqualTo("notype");
                assertThat(result.get(1).getFunctionId()).isEqualTo(2L);
                assertThat(result.get(1).getRuntime().getName()).isEqualTo("java11");
                assertThat(result.get(1).getFunctionType().getName()).isEqualTo("notype");
                testContext.completeNow();
            }), throwable -> testContext.failNow("method has thrown exception"));
    }
}
