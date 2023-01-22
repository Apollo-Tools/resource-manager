package at.uibk.dps.rm.handler.function;


import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import at.uibk.dps.rm.testutil.TestObjectProvider;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class FunctionCheckerTest {


    private FunctionChecker functionChecker;

    @Mock
    private FunctionService functionService;



    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionChecker = new FunctionChecker(functionService);
    }

    @Test
    void checkForDuplicateEntityFalse(VertxTestContext testContext) {
        String name = "func";
        long runtimeId = 1L;
        Function function = TestObjectProvider.createFunction(1L, name, "true", runtimeId);

        when(functionService.existsOneByNameAndRuntimeId(name, runtimeId)).thenReturn(Single.just(false));

        functionChecker.checkForDuplicateEntity(JsonObject.mapFrom(function))
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(functionService).existsOneByNameAndRuntimeId(name, runtimeId);
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityTrue(VertxTestContext testContext) {
        String name = "func";
        long runtimeId = 1L;
        Function function = TestObjectProvider.createFunction(1L, name, "true", runtimeId);

        when(functionService.existsOneByNameAndRuntimeId(name, runtimeId)).thenReturn(Single.just(true));

        functionChecker.checkForDuplicateEntity(JsonObject.mapFrom(function))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateNoDuplicateWithKeysNotExists(VertxTestContext testContext) {
        String name = "func";
        long functionId = 1L, runtimeId = 2L;
        Function function = TestObjectProvider.createFunction(functionId, name, "true", runtimeId);
        JsonObject entity = JsonObject.mapFrom(function);

        when(functionService.existsOneByNameAndRuntimeIdExcludeEntity(functionId, name, runtimeId))
            .thenReturn(Single.just(false));

        functionChecker.checkUpdateNoDuplicate(entity, entity)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("function_id")).isEqualTo(1L);
                    assertThat(result.getString("name")).isEqualTo(name);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
            );
    }

    @Test
    void checkUpdateNoDuplicateWithKeysExists(VertxTestContext testContext) {
        String name = "func";
        long functionId = 1L, runtimeId = 2L;
        Function function = TestObjectProvider.createFunction(functionId, name, "true", runtimeId);
        JsonObject entity = JsonObject.mapFrom(function);

        when(functionService.existsOneByNameAndRuntimeIdExcludeEntity(functionId, name, runtimeId))
            .thenReturn(Single.just(true));

        functionChecker.checkUpdateNoDuplicate(entity, entity)
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkUpdateNoDuplicateWithoutName(VertxTestContext testContext) {
        String name = "func";
        long functionId = 1L, runtimeId = 2L;
        Function function = TestObjectProvider.createFunction(functionId, name, "true", runtimeId);
        JsonObject entity = JsonObject.mapFrom(function);
        JsonObject body = JsonObject.mapFrom(function);
        body.remove("name");

        functionChecker.checkUpdateNoDuplicate(body, entity)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("function_id")).isEqualTo(1L);
                    assertThat(result.getString("name")).isEqualTo(name);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
            );
    }

    @Test
    void checkUpdateNoDuplicateWithoutRuntime(VertxTestContext testContext) {
        String name = "func";
        long functionId = 1L, runtimeId = 2L;
        Function function = TestObjectProvider.createFunction(functionId, name, "true", runtimeId);
        JsonObject entity = JsonObject.mapFrom(function);
        JsonObject body = JsonObject.mapFrom(function);
        body.remove("runtime");

        functionChecker.checkUpdateNoDuplicate(body, entity)
            .subscribe(result -> testContext.verify(() -> {
                    assertThat(result.getLong("function_id")).isEqualTo(1L);
                    assertThat(result.getString("name")).isEqualTo(name);
                    testContext.completeNow();
                }),
                throwable -> testContext.verify(() -> fail("method did throw exception " + throwable.getMessage()))
            );
    }
}
