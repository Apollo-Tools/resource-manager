package at.uibk.dps.rm.util;

import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class HttpHelper {

    public static Maybe<Long> getLongPathParam(RoutingContext rc, String pathParam) {
        return Maybe.just(rc.pathParam(pathParam))
            .map(Long::parseLong);
    }
}
