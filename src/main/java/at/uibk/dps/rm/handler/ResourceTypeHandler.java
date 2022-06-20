package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.service.database.DatabaseService;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.MaybeHelper;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.serviceproxy.ServiceProxyBuilder;

public class ResourceTypeHandler {

    private final DatabaseService resourceTypeService;

    public ResourceTypeHandler(Vertx vertx) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx.getDelegate())
                .setAddress("database-service-address");
        resourceTypeService = builder.build(DatabaseService.class);
    }

    public Completable post(RoutingContext rc) {
        return rc.response().end();
    }

    public Completable all(RoutingContext rc) {
        // TODO: Not working ???
        return Completable.fromCompletionStage(resourceTypeService.findAll("ResourceType").toCompletionStage());
    }

    public Completable get(RoutingContext rc) {
        return rc.response().end();
    }

    public Completable patch(RoutingContext rc) {
        return rc.response().end();
    }

    public Completable delete(RoutingContext rc) {
        return rc.response().end();
    }
}
