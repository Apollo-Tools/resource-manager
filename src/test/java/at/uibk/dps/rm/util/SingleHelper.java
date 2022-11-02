package at.uibk.dps.rm.util;

import io.reactivex.rxjava3.core.Single;
import io.vertx.core.Future;
import io.vertx.rxjava3.impl.AsyncResultSingle;

public class SingleHelper<T> {
    public Single<T> getEmptySingle() {
        T emptyObject = null;
        // As found in "build/generated/sources/annotationProcessor/java/Main/at.uibk.dps.rm.service/rxjava3.database/metric/MetricService"
        return AsyncResultSingle.toSingle(Future.succeededFuture(emptyObject), value -> value);
    }
}
