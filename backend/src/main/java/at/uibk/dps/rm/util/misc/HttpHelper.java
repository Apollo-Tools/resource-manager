package at.uibk.dps.rm.util.misc;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

/**
 * This class is a utility class for http requests.
 */
@UtilityClass
public class HttpHelper {

    /**
     * Get a path parameter of type long from a routing context by its name.
     *
     * @param rc the routing context
     * @param pathParam the name of the path parameter
     * @return a Single that emits the value of the path parameter
     */
    public static Single<Long> getLongPathParam(RoutingContext rc, String pathParam) {
        return Single.just(rc.pathParam(pathParam))
            .map(Long::parseLong);
    }
}