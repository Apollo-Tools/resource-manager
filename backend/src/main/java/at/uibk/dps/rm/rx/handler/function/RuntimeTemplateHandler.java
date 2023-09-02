package at.uibk.dps.rm.rx.handler.function;

import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.rx.handler.ValidationHandler;
import at.uibk.dps.rm.rx.service.rxjava3.database.function.RuntimeService;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the runtime templates.
 *
 * @author matthi-g
 */
public class RuntimeTemplateHandler extends ValidationHandler {

    private final RuntimeService runtimeService;

    /**
     * Create an instance.
     */
    public RuntimeTemplateHandler(RuntimeService runtimeService) {
        super(runtimeService);
        this.runtimeService = runtimeService;
    }

    protected Single<JsonObject> getOne(RoutingContext rc) {
        Vertx vertx = Vertx.currentContext().owner();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(runtimeService::findOne)
            .flatMap(jsonObject -> {
                String templatePath = jsonObject.getString("template_path");
                return vertx.fileSystem().exists(templatePath)
                    .flatMap(result -> {
                        if (!result || templatePath.isEmpty()) {
                            return Single.error(new NotFoundException("runtime template not found"));
                        }
                        return vertx.fileSystem().readFile(templatePath);
                    })
                    .map(buffer -> buffer.getString(0, buffer.length()));
            })
            .map(templateContent -> {
                JsonObject result = new JsonObject();
                result.put("template", templateContent);
                return result;
            });
    }
}
