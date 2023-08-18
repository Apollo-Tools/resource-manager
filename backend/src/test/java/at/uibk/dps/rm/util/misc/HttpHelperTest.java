package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.exception.BadInputException;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.rxjava3.ext.web.RoutingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

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
        when(rc.pathParam("id")).thenReturn("10");

        HttpHelper.getLongPathParam(rc, "id")
                .subscribe(result -> testContext.verify(() -> {
                    assertThat(result).isEqualTo(10L);
                    testContext.completeNow();
                }));
    }

    @Test
    void getLongPathParamInValid(VertxTestContext testContext) {
        when(rc.pathParam("id")).thenReturn("test");

        HttpHelper.getLongPathParam(rc, "id")
            .subscribe(result -> testContext.verify(() -> fail("method did not throw exception")),
                throwable -> testContext.verify(() -> {
                    assertThat(throwable).isInstanceOf(BadInputException.class);
                    assertThat(throwable.getMessage()).isEqualTo("path parameter is not a number");
                    testContext.completeNow();
            }));
    }
}
