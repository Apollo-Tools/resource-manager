package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the runtime templates.
 *
 * @author matthi-g
 */
public class RuntimeTemplateHandler extends ValidationHandler {

    private final FileSystemChecker fileSystemChecker;

    /**
     * Create an instance from the runtimeChecker and fileSystemChecker
     *
     * @param runtimeChecker the runtime checker
     * @param fileSystemChecker the file system checker
     */
    public RuntimeTemplateHandler(RuntimeChecker runtimeChecker, FileSystemChecker fileSystemChecker) {
        super(runtimeChecker);
        this.fileSystemChecker = fileSystemChecker;
    }

    @Override
    protected Single<JsonObject> getOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne)
            .flatMap(jsonObject -> {
                String templatePath = jsonObject.getString("template_path");
                return fileSystemChecker.checkTemplatePathExists(templatePath)
                    .andThen(Single.defer(() -> fileSystemChecker.checkGetFileTemplate(templatePath)));
            })
            .map(templateContent -> {
                JsonObject result = new JsonObject();
                result.put("template", templateContent);
                return result;
            });
    }
}
