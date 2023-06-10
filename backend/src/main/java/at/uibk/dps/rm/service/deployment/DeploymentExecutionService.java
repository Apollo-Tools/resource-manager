package at.uibk.dps.rm.service.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.deployment.DeploymentCredentials;
import at.uibk.dps.rm.entity.deployment.FunctionsToDeploy;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDAO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDAO;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
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
     * @return a Future that emits the functions to deploy
     */
    Future<FunctionsToDeploy> packageFunctionsCode(DeployResourcesDAO deployRequest);

    /**
     * Setup the terraform modules that are necessary for the deployment. The modules are
     * grouped by region and resource provider.
     *
     * @param deployRequest the data that is necessary for the deployment
     * @return a Future that emits the credentials that are necessary for the terraform deployment
     */
    Future<DeploymentCredentials> setUpTFModules(DeployResourcesDAO deployRequest);

    /**
     * Get the necessary credentials for termination.
     *
     * @param terminateRequest the data that is necessary for termination
     * @return the credentials that are necessary for termination
     */
    Future<DeploymentCredentials> getNecessaryCredentials(TerminateResourcesDAO terminateRequest);

    /**
     * Delete all terraform directories that exist for a deployment.
     *
     * @param deploymentId the id of the deployment
     * @return an empty Future
     */
    Future<Void> deleteTFDirs(long deploymentId);
}
