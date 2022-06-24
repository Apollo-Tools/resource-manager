package at.uibk.dps.rm.handler.Resource;

import at.uibk.dps.rm.service.resource.ResourceService;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class ResourceHandler {
    private final ResourceService resourceService;

    public ResourceHandler(Vertx vertx) {
        resourceService = ResourceService.createProxy(vertx.getDelegate(),
            "resource-service-address");
    }

    public void post(RoutingContext rc) {
        rc.end();
    }

    public void get(RoutingContext rc) {
        rc.end();
    }

    public void all(RoutingContext rc) {
        rc.end();
    }

    public void patch(RoutingContext rc) {
        rc.end();
    }

    public void delete(RoutingContext rc) {
        rc.end();
    }
}
