package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.service.rxjava3.ServiceInterface;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public abstract class RequestHandler {

    protected final ServiceInterface service;

    protected RequestHandler(ServiceInterface service) {
        this.service = service;
    }

    public abstract Disposable post(RoutingContext rc);

    public abstract Disposable patch(RoutingContext rc);

    public Disposable get(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(this::checkFindOne)
            .subscribe(result -> ResultHandler.handleGetOneRequest(rc, result),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    public Disposable all(RoutingContext rc) {
        return service.findAll()
            .subscribe(result -> ResultHandler.handleGetAllRequest(rc, result),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    public Disposable delete(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> checkExistsOne(id)
                .andThen(Single.just(id)))
            .flatMapCompletable(this::submitDelete)
            .subscribe(() -> ResultHandler.handleSaveAllUpdateDeleteRequest(rc),
                throwable -> ErrorHandler.handleRequestError(rc, throwable));
    }

    protected Single<JsonObject> submitCreate(JsonObject requestBody) {
        return service.save(requestBody);
    }


    protected Completable submitUpdate(JsonObject requestBody, JsonObject entity) {
        for (String field : requestBody.fieldNames()) {
            entity.put(field, requestBody.getValue(field));
        }
        return service.update(entity);
    }

    protected Completable submitDelete(long id) {
        return service.delete(id);
    }

    protected Single<JsonObject> checkFindOne(long id) {
        Single<JsonObject> findOneById = service.findOne(id);
        return ErrorHandler.handleFindOne(findOneById);
    }

    protected Completable checkExistsOne(long id) {
        Single<Boolean> existsOneById = service.existsOneById(id);
        return ErrorHandler.handleExistsOne(existsOneById).ignoreElement();
    }
}
