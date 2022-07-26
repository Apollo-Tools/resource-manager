package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.exception.UsedByOtherEntityException;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class ErrorHandler {

    public static Single<JsonObject> handleFindOne(Single<JsonObject> handler) {
        return handler
            .map(result -> {
                if (result == null) {
                    throw new NotFoundException();
                }
                return result;
            });
    }

    public static Single<Boolean> handleExistsOne(Single<Boolean> handler) {
        return handler
            .map(result -> {
                if (!result) {
                    throw new NotFoundException();
                }
                return true;
            });
    }

    public static Single<Boolean> handleDuplicates(Single<Boolean> handler) {
        return handler
            .map(result -> {
                if (result) {
                    throw new AlreadyExistsException();
                }
                return false;
            });
    }

    public static Single<Boolean> handleUsedByOtherEntity(Single<Boolean> handler) {
        return handler
            .map(result -> {
                if (result) {
                    throw new UsedByOtherEntityException();
                }
                return false;
            });
    }
}
