package at.uibk.dps.rm.rx.handler.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.exception.WrongFileTypeException;
import at.uibk.dps.rm.util.validation.EntityNameValidator;
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
        checks.add(EntityNameValidator.checkName(functionName, Function.class));
        Completable.merge(checks)
            .doOnError(handleError(rc, !isJsonInput))
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
            .doOnError(handleError(rc, !isJsonInput))
            .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }

    /**
     * Check if a file hast the correct file type. The only supported file type are .zip files.
     *
     * @param file the file
     * @return a Completable if the file type is correct, else a {@link WrongFileTypeException} is
     * thrown
     */
    private Completable checkFileType(FileUpload file) {
        if (file.fileName().endsWith(".zip")) {
            return Completable.complete();
        }
        return Completable.error(new WrongFileTypeException());
    }

    /**
     * Handle any errors that occur during validation. This is necessary to delete any unwanted
     * files.
     *
     * @param rc the routing context
     * @param isFileUpload indicates if the request contains a file upload
     * @return a consumer that expects a throwable as input
     */
    private static Consumer<Throwable> handleError(RoutingContext rc, boolean isFileUpload) {
        return throwable -> {
            if (!isFileUpload) {
                Vertx.currentContext().owner()
                    .fileSystem().deleteBlocking(rc.fileUploads().get(0).uploadedFileName());
            }
        };
    }
}
