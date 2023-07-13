package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.dto.deployment.FunctionResourceIds;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionService;
import at.uibk.dps.rm.testutil.objectprovider.TestFunctionProvider;
import at.uibk.dps.rm.util.serialization.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.when;

/**
 * Implements tests for the {@link FunctionChecker} class.
 *
 * @author matthi-g
 */
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
    void checkExistAllByIds(VertxTestContext testContext) {
        FunctionResourceIds fr1 = TestFunctionProvider.createFunctionResourceIds(1L, 1L);
        FunctionResourceIds fr2 = TestFunctionProvider.createFunctionResourceIds(2L, 1L);
        FunctionResourceIds fr3 = TestFunctionProvider.createFunctionResourceIds(3L, 2L);

        when(functionService.existsAllByIds(Set.of(1L, 2L, 3L))).thenReturn(Single.just(true));


        functionChecker.checkExistAllByIds(List.of(fr1, fr2, fr3))
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method has thrown exception"))
            );
        testContext.completeNow();
    }

    @Test
    void checkExistAllByIdsNotExists(VertxTestContext testContext) {
        FunctionResourceIds fr1 = TestFunctionProvider.createFunctionResourceIds(1L, 1L);
        FunctionResourceIds fr2 = TestFunctionProvider.createFunctionResourceIds(2L, 1L);
        FunctionResourceIds fr3 = TestFunctionProvider.createFunctionResourceIds(3L, 2L);

        when(functionService.existsAllByIds(Set.of(1L, 2L, 3L))).thenReturn(Single.just(false));


        functionChecker.checkExistAllByIds(List.of(fr1, fr2, fr3))
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }
}
