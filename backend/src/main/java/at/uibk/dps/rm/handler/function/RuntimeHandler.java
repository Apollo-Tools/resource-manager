package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.service.rxjava3.database.function.RuntimeService;
import at.uibk.dps.rm.service.rxjava3.util.FilePathService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class RuntimeHandler extends ValidationHandler {

    private final FileSystemChecker fileSystemChecker;

    public RuntimeHandler(RuntimeService runtimeService, FilePathService filePathService) {
        super(new RuntimeChecker(runtimeService));
        this.fileSystemChecker = new FileSystemChecker(filePathService);
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return entityChecker.checkForDuplicateEntity(requestBody)
            .andThen(checkTemplatePathExists(requestBody))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }

    @Override
    protected Completable updateOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(entityChecker::checkFindOne)
            .flatMap(result -> checkTemplatePathExists(requestBody)
                .andThen(Single.just(result)))
            .flatMapCompletable(result -> entityChecker.submitUpdate(requestBody, result));
    }

    protected Completable checkTemplatePathExists(JsonObject requestBody) {
        if (requestBody.containsKey("template_path")) {
            String templatePath = requestBody.getString("template_path");
            return fileSystemChecker.checkTemplatePathExists(templatePath);
        }
        return Completable.complete();
    }
}
