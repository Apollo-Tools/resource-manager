package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.SetupTFModulesOutputDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * The interface of the service proxy for deployment operations.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface DeploymentExecutionService {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static DeploymentExecutionService create() {
        return new DeploymentExecutionServiceImpl();
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static DeploymentExecutionService createProxy(Vertx vertx) {
        return new DeploymentExecutionServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(
            "deployment-execution"));
    }

    /**
     * Setup and package the code of all functions to deploy.
     *
     * @param deployRequest the data that is necessary for the deployment
     * @param resultHandler receives the functions to deploy if the packaging was successful else it
     *                      receives an error
     */
    void packageFunctionsCode(DeployResourcesDTO deployRequest, Handler<AsyncResult<FunctionsToDeploy>> resultHandler);

    /**
     * Set up the terraform modules that are necessary for the deployment. The modules are
     * grouped by region and resource provider.
     *
     * @param deployRequest the data that is necessary for the deployment
     * @param resultHandler receives the credentials necessary for the deployment if the
     *                      setup was successful else it receives an error
     */
    void setUpTFModules(DeployResourcesDTO deployRequest, Handler<AsyncResult<SetupTFModulesOutputDTO>> resultHandler);

    /**
     * Get the necessary credentials for termination.
     *
     * @param terminateRequest the data that is necessary for termination
     * @param resultHandler receives the credentials necessary for termination
     */
    void getNecessaryCredentials(TerminateResourcesDTO terminateRequest,
        Handler<AsyncResult<DeploymentCredentials>> resultHandler);
}
