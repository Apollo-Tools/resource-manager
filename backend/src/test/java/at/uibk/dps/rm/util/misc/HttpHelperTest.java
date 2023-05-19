package at.uibk.dps.rm.util.misc;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

/**
 * Implements tests for the {@link HttpHelper} class.
 *
 * @author matthi-g
 */
@ExtendWith(VertxExtension.class)
@ExtendWith(MockitoExtension.class)
public class HttpHelperTest {

    @Mock
    RoutingContext rc;

    @Test
    void getLongPathParamValid(VertxTestContext testContext) {
        doReturn("10").when(rc).pathParam(anyString());

        HttpHelper.getLongPathParam(rc, "id")
                .subscribe(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(10L);
                    testContext.completeNow();
                }));
    }

    @Test
    void getLongPathParamInValid(VertxTestContext testContext) {
        doReturn("test").when(rc).pathParam(anyString());

        HttpHelper.getLongPathParam(rc, "id")
                .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                        throwable -> testContext.verify(() -> {
                            assertThat(throwable).isInstanceOf(NumberFormatException.class);
                            testContext.completeNow();
                        }));
    }
}
