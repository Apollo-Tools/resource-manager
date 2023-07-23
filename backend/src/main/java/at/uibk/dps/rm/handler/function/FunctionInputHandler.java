package at.uibk.dps.rm.handler.function;

import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.WrongFileTypeException;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.functions.Consumer;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.MultiMap;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.FileUpload;
import io.vertx.rxjava3.ext.web.RoutingContext;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to validate the inputs of the function endpoint and fails the context if violations are found.
 *
 * @author matthi-g
 */
@UtilityClass
public class FunctionInputHandler {

    /**
     * Validate the name and filetype of a function that should be created.
     *
     * @param rc the routing context
     */
    public static void validateAddFunctionRequest(RoutingContext rc) {
        String functionName;
        List<Completable> checks = new ArrayList<>();
        boolean isJsonInput = rc.request().headers().get("Content-Type").equals("application/json");
        if (isJsonInput) {
            JsonObject requestBody = rc.body().asJsonObject();
            functionName = requestBody.getString("name");
        } else {
            MultiMap attributes = rc.request().formAttributes();
            functionName = attributes.get("name");
            FileUpload file = rc.fileUploads().get(0);
            checks.add(checkFileType(file));
        }
        checks.add(checkFunctionName(functionName));
        Completable.merge(checks)
            .doOnError(handleError(rc, isJsonInput))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }

    /**
     * Validate the filetype of a function that should be updated.
     *
     * @param rc the routing context
     */
    public static void validateUpdateFunctionRequest(RoutingContext rc) {
        Completable checkFileType = Completable.complete();
        boolean isJsonInput = rc.request().headers().get("Content-Type").equals("application/json");
        if (!isJsonInput) {
            FileUpload file = rc.fileUploads().get(0);
            checkFileType = checkFileType(file);
        }
        checkFileType
            .doOnError(handleError(rc, isJsonInput))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }

    private Completable checkFunctionName(String functionName) {
        if (functionName.matches("^[a-z0-9]+$")) {
            return Completable.complete();
        }
        return Completable.error(new BadInputException("invalid function name"));
    }

    private Completable checkFileType(FileUpload file) {
        if (file.fileName().endsWith(".zip")) {
            return Completable.complete();
        }
        return Completable.error(new WrongFileTypeException());
    }

    private static Consumer<Throwable> handleError(RoutingContext rc, boolean isJsonInput) {
        return throwable -> {
            if (!isJsonInput) {
                Vertx.currentContext().owner()
                    .fileSystem().deleteBlocking(rc.fileUploads().get(0).uploadedFileName());
            }
        };
    }
}
