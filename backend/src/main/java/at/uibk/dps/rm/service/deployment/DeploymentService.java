package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.entity.dto.TerminateResourcesRequest;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

@ProxyGen
@VertxGen
public interface DeploymentService {

    @Generated
    @GenIgnore
    static DeploymentService create() {
        return new DeploymentServiceImpl();
    }

    @Generated
    static DeploymentService createProxy(Vertx vertx) {
        return new DeploymentServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress("deployment"));
    }

    Future<FunctionsToDeploy> packageFunctionsCode(DeployResourcesRequest deployRequest);

    Future<DeploymentCredentials> setUpTFModules(DeployResourcesRequest deployRequest);

    Future<DeploymentCredentials> getNecessaryCredentials(TerminateResourcesRequest terminateRequest);

    Future<Void> deleteTFDirs(long reservationId);
}
