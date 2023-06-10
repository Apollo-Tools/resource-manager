package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

/**
 * The interface of the service proxy for the service_deployment entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ServiceDeploymentService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ServiceDeploymentService create(ServiceDeploymentRepository repository) {
        return new ServiceDeploymentServiceImpl(repository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ServiceDeploymentService createProxy(Vertx vertx) {
        return new ServiceDeploymentServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ServiceDeployment.class));
    }

    /**
     * Find all service deployments that belong to a deployment.
     *
     * @param deploymentId the id of the deployment
     * @return a Future that emits all service deployments as JsonArray
     */
    Future<JsonArray> findAllByDeploymentId(long deploymentId);

    /**
     * Check if a service deployment is ready for startup or termination
     *
     * @param deploymentId the id of the deployment
     * @param resourceDeploymentId the id of the resource deployment
     * @param accountId the account id of the creator
     * @return a Future that emits true if the function exists, else false
     */
    Future<Boolean> existsReadyForContainerStartupAndTermination(long deploymentId,
        long resourceDeploymentId, long accountId);
}
