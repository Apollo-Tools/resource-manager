package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.service.rxjava3.database.function.RuntimeService;
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
public class RuntimeCheckerTest {

    private RuntimeChecker runtimeChecker;

    @Mock
    private RuntimeService runtimeService;



    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        runtimeChecker = new RuntimeChecker(runtimeService);
    }

    @Test
    void checkForDuplicateEntityFalse(VertxTestContext testContext) {
        long runtimeId = 1L;
        String name = "python3.9";
        Runtime runtime = TestObjectProvider.createRuntime(runtimeId, name);

        when(runtimeService.existsOneByName(name)).thenReturn(Single.just(false));

        runtimeChecker.checkForDuplicateEntity(JsonObject.mapFrom(runtime))
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(runtimeService).existsOneByName(name);
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityTrue(VertxTestContext testContext) {
        long runtimeId = 1L;
        String name = "python3.9";
        Runtime runtime = TestObjectProvider.createRuntime(runtimeId, name);

        when(runtimeService.existsOneByName(name)).thenReturn(Single.just(true));

        runtimeChecker.checkForDuplicateEntity(JsonObject.mapFrom(runtime))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }
}
