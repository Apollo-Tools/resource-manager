package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.entity.dto.resource.RuntimeEnum;
import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.util.misc.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.ext.web.FileUpload;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the function entity.
 *
 * @author matthi-g
 */
public class FunctionHandler extends ValidationHandler {

    private final FunctionChecker functionChecker;

    private final RuntimeChecker runtimeChecker;

    /**
     * Create an instance from the functionChecker and runtimeChecker.
     *
     * @param functionChecker the function checker
     * @param runtimeChecker the runtime checker
     */
    public FunctionHandler(FunctionChecker functionChecker, RuntimeChecker runtimeChecker) {
        super(functionChecker);
        this.functionChecker = functionChecker;
        this.runtimeChecker = runtimeChecker;
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody;
        boolean isFile;
        if (rc.request().headers().get("Content-Type").equals("application/json")) {
            isFile = false;
            requestBody = rc.body().asJsonObject();
        } else {
            isFile = true;
            MultiMap attributes = rc.request().formAttributes();
            requestBody = new JsonObject();
            requestBody.put("name", attributes.get("name"));
            requestBody.put("runtime", new JsonObject(attributes.get("runtime")));
            FileUpload file = rc.fileUploads().get(0);
            String[] filePath = file.uploadedFileName()
                .replace("\\", "/")
                .split("/");
            requestBody.put("code", filePath[filePath.length - 1]);
        }
        requestBody.put("is_file", isFile);
        return runtimeChecker.checkFindOne(requestBody
                .getJsonObject("runtime")
                .getLong("runtime_id"))
            .flatMapCompletable(runtime -> {
                RuntimeEnum selectedRuntime = RuntimeEnum.fromRuntime(runtime.mapTo(Runtime.class));
                if (!isFile && !selectedRuntime.equals(RuntimeEnum.PYTHON38)) {
                    return Completable.error(new BadInputException("runtime only supports zip archives"));
                }
                return Completable.complete();
            })
            .andThen(entityChecker.checkForDuplicateEntity(requestBody))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }

    @Override
    protected Completable updateOne(RoutingContext rc) {
        JsonObject requestBody;
        boolean isFile;
        if (rc.request().headers().get("Content-Type").equals("application/json")) {
            isFile = false;
            requestBody = rc.body().asJsonObject();
        } else {
            isFile = true;
            requestBody = new JsonObject();
            FileUpload file = rc.fileUploads().get(0);
            String[] filePath = file.uploadedFileName()
                .replace("\\", "/")
                .split("/");
            requestBody.put("code", filePath[filePath.length - 1]);
        }
        requestBody.put("is_file", isFile);
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> entityChecker.submitUpdate(id, requestBody));
    }

    @Override
    protected Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMapCompletable(id -> entityChecker.checkFindOne(id)
                .flatMapCompletable(function -> checkDeleteEntityIsUsed(function)
                    .andThen(functionChecker.submitDelete(id, function)))
            );
    }
}
