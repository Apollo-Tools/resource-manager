package at.uibk.dps.rm.service.deployment;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface DeploymentService {
    @GenIgnore
    static DeploymentService create() {
        return new DeploymentServiceImpl();
    }

    static DeploymentService createProxy(Vertx vertx, String address) {
        return new DeploymentServiceVertxEBProxy(vertx, address);
    }

    Future<Long> deploy(JsonObject data);

    Future<Void> terminate(long resourceId);
}
