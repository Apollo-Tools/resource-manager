package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.handler.ResultHandler;
import io.reactivex.rxjava3.disposables.Disposable;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceDeploymentHandler {
    private final ResourceHandler resourceHandler;

    public ResourceDeploymentHandler(ResourceHandler resourceHandler) {
        this.resourceHandler = resourceHandler;
    }

    public Disposable getResourcesBySLOs(RoutingContext rc) {
        return ResultHandler.handleGetOneRequest(rc, resourceHandler.getResourceBySLOs(rc));
    }
}
