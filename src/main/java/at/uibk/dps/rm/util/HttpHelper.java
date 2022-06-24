package at.uibk.dps.rm.util;

import io.reactivex.rxjava3.core.Maybe;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class HttpHelper {
    private final static String INVALID_PATH_PARAM_MSG = "invalid path parameter";

    public static Maybe<Long> getLongPathParam(RoutingContext rc, String pathParam) {
        return Maybe.just(rc.pathParam(pathParam))
            .map(Long::parseLong)
            .onErrorComplete(throwable -> {
                rc.fail(400, new Throwable(INVALID_PATH_PARAM_MSG));
                return true;
            });
    }
}
