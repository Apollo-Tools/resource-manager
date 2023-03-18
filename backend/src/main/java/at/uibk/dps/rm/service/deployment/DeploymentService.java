package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

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

    Future<FunctionsToDeploy> packageFunctionsCode(DeployResourcesRequest deployRequest);

    Future<DeploymentCredentials> setUpTFModules(DeployResourcesRequest deployRequest);

    Future<Integer> deploy(DeployResourcesRequest deployRequest);

    Future<Void> terminate(long resourceId);
}
