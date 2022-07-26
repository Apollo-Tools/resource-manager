package at.uibk.dps.rm.handler;

import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class RequestHandler {

    private final ValidationHandler validationHandler;

    public RequestHandler(ValidationHandler validationHandler) {
        this.validationHandler = validationHandler;
    }

    public Disposable getRequest(RoutingContext rc) {
        return ResultHandler.handleGetOneRequest(rc, validationHandler.getOne(rc));
    }

    public Disposable getAllRequest(RoutingContext rc) {
        return ResultHandler.handleGetAllRequest(rc, validationHandler.getAll(rc));
    }

    public Disposable postRequest(RoutingContext rc) {
        return ResultHandler.handleSaveOneRequest(rc, validationHandler.postOne(rc));
    }

    public Disposable postAllRequest(RoutingContext rc) {
        return ResultHandler.handleSaveAllUpdateDeleteRequest(rc, validationHandler.postAll(rc));
    }

    public Disposable patchRequest(RoutingContext rc) {
        return ResultHandler.handleSaveAllUpdateDeleteRequest(rc, validationHandler.updateOne(rc));
    }

    public Disposable deleteRequest(RoutingContext rc) {
        return ResultHandler.handleSaveAllUpdateDeleteRequest(rc, validationHandler.deleteOne(rc));
    }


}
