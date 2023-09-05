package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the runtime templates.
 *
 * @author matthi-g
 */
@Deprecated
public class RuntimeTemplateHandler extends ValidationHandler {

    /**
     * Create an instance from the runtimeChecker
     *
     * @param runtimeChecker the runtime checker
     */
    public RuntimeTemplateHandler(RuntimeChecker runtimeChecker) {
        super(runtimeChecker);
    }

    @Override
    protected Single<JsonObject> getOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne)
            .map(templateContent -> {
                JsonObject result = new JsonObject();
                result.put("template", templateContent);
                return result;
            });
    }
}
