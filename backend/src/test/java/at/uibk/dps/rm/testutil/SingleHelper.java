package at.uibk.dps.rm.testutil;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.rxjava3.impl.AsyncResultSingle;
import lombok.experimental.UtilityClass;

/**
 * Helper class for Singles in tests.
 *
 * @author matthi-g
 */
@UtilityClass
public class SingleHelper {

    /**
     * Get a Single that emits null.
     *
     * @return a Single that emits null
     * @param <T> the type of the emitted item
     */
    public static <T> Single<T> getEmptySingle() {
        T emptyObject = null;
        // As found in "build/generated/sources/annotationProcessor/java/Main/at.uibk.dps.rm.service/rxjava3.database/metric/MetricService"
        return AsyncResultSingle.toSingle(Future.succeededFuture(emptyObject), value -> value);
    }
}
