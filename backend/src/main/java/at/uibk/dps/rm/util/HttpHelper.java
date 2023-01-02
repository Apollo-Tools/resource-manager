package at.uibk.dps.rm.util;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class HttpHelper {

    public static Single<Long> getLongPathParam(RoutingContext rc, String pathParam) {
        return Single.just(rc.pathParam(pathParam))
            .map(Long::parseLong);
    }
}
