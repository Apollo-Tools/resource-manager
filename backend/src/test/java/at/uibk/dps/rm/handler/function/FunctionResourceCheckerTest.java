package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.service.rxjava3.database.function.FunctionResourceService;
import at.uibk.dps.rm.util.JsonMapperConfig;
import io.reactivex.rxjava3.core.Single;
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
public class FunctionResourceCheckerTest {

    private FunctionResourceChecker functionResourceChecker;

    @Mock
    private FunctionResourceService functionResourceService;

    @BeforeEach
    void initTest() {
        JsonMapperConfig.configJsonMapper();
        functionResourceChecker = new FunctionResourceChecker(functionResourceService);
    }

    @Test
    void checkForDuplicateEntityByFunctionAndResourceNotExists(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(functionResourceService.existsOneByFunctionAndResource(functionId, resourceId))
            .thenReturn(Single.just(false));

        functionResourceChecker.checkForDuplicateByFunctionAndResource(functionId, resourceId)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(functionResourceService).existsOneByFunctionAndResource(functionId, resourceId);
        testContext.completeNow();
    }

    @Test
    void checkForDuplicateEntityExists(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(functionResourceService.existsOneByFunctionAndResource(functionId, resourceId))
            .thenReturn(Single.just(true));

        functionResourceChecker.checkForDuplicateByFunctionAndResource(functionId, resourceId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(AlreadyExistsException.class);
                    testContext.completeNow();
                })
            );
    }

    @Test
    void checkFunctionResourceExistsByFunctionAndResourceExists(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(functionResourceService.existsOneByFunctionAndResource(functionId, resourceId))
            .thenReturn(Single.just(true));

        functionResourceChecker.checkFunctionResourceExistsByFunctionAndResource(functionId, resourceId)
            .blockingSubscribe(() -> {
                },
                throwable -> testContext.verify(() -> fail("method did throw exception"))
            );

        verify(functionResourceService).existsOneByFunctionAndResource(functionId, resourceId);
        testContext.completeNow();
    }

    @Test
    void checkMetricValueExistsByResourceAndMetricNotExists(VertxTestContext testContext) {
        long functionId = 1L;
        long resourceId = 1L;

        when(functionResourceService.existsOneByFunctionAndResource(functionId, resourceId))
            .thenReturn(Single.just(false));

        functionResourceChecker.checkFunctionResourceExistsByFunctionAndResource(functionId, resourceId)
            .blockingSubscribe(() -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(NotFoundException.class);
                    testContext.completeNow();
                })
            );
    }


}
