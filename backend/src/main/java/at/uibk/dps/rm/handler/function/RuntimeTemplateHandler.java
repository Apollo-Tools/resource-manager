package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class RuntimeTemplateHandler extends ValidationHandler {

    private final FileSystemChecker fileSystemChecker;

    public RuntimeTemplateHandler(ServiceProxyProvider serviceProxyProvider) {
        super(new RuntimeChecker(serviceProxyProvider.getRuntimeService()));
        this.fileSystemChecker = new FileSystemChecker(serviceProxyProvider.getFilePathService());
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
