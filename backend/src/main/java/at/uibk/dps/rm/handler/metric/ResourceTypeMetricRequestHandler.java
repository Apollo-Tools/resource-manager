package at.uibk.dps.rm.handler.metric;

import at.uibk.dps.rm.handler.RequestHandler;
import at.uibk.dps.rm.handler.ResultHandler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceTypeMetricRequestHandler extends RequestHandler {

    private final ResourceTypeMetricHandler validationHandler;

    public ResourceTypeMetricRequestHandler(ResourceTypeMetricHandler validationHandler) {
        super(validationHandler);
        this.validationHandler = validationHandler;
    }

    @Override
    public Disposable getAllRequest(RoutingContext rc) {
        return ResultHandler.handleGetOneRequest(rc, validationHandler.getAllMetrics(rc));
    }
}
