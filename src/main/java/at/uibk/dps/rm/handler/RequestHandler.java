package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.service.ServiceInterface;
import at.uibk.dps.rm.util.HttpHelper;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public abstract class RequestHandler {

    protected final ServiceInterface service;

    protected RequestHandler(ServiceInterface service) {
        this.service = service;
    }

    public abstract void post(RoutingContext rc);

    public abstract void patch(RoutingContext rc);

    public void get(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "id")
            .subscribe(
                id ->  service.findOne(id)
                    .onComplete(
                        handler -> ResultHandler.handleGetOneRequest(rc, handler)),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    public void all(RoutingContext rc) {
        service.findAll()
            .onComplete(handler -> ResultHandler.handleGetAllRequest(rc, handler));
    }

    public void delete(RoutingContext rc) {
        HttpHelper.getLongPathParam(rc, "id")
            .subscribe(
                id -> checkDeleteEntityExists(rc, id),
                throwable -> rc.fail(500, throwable))
            .dispose();
    }

    protected void submitCreate(RoutingContext rc, JsonObject requestBody) {
        service.save(requestBody)
            .onComplete(handler -> ResultHandler.handleSaveRequest(rc, handler));
    }


    protected void submitUpdate(RoutingContext rc, JsonObject requestBody,
        JsonObject entity) {
        for (String field : requestBody.fieldNames()) {
            entity.put(field, requestBody.getValue(field));
        }
        service.update(entity)
            .onComplete(updateHandler -> ResultHandler.handleSaveAllUpdateDeleteRequest(rc, updateHandler));
    }

    protected void submitDelete(RoutingContext rc, long id) {
        service.delete(id)
            .onComplete(deleteHandler -> ResultHandler.handleSaveAllUpdateDeleteRequest(rc, deleteHandler));
    }

    protected Future<JsonObject> checkFindOne(RoutingContext rc, long id) {
        return service.findOne(id)
            .onComplete(updateHandler -> ErrorHandler.handleFindOne(rc, updateHandler));
    }

    protected Future<Boolean> checkExistsOne(RoutingContext rc, long id) {
        return service.existsOneById(id)
            .onComplete(existsHandler -> ErrorHandler.handleExistsOne(rc, existsHandler));
    }

    protected void checkDeleteEntityExists(RoutingContext rc, long id) {
        service.existsOneById(id)
            .onComplete(findHandler -> ErrorHandler.handleExistsOne(rc, findHandler))
            .onComplete(findHandler -> {
                if (!rc.failed()) {
                    submitDelete(rc, id);
                }
            });
    }
}
