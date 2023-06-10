package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.repository.deployment.FunctionDeploymentRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

/**
 * The interface of the service proxy for the function_deployment entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface FunctionDeploymentService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static FunctionDeploymentService create(FunctionDeploymentRepository repository) {
        return new FunctionDeploymentServiceImpl(repository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static FunctionDeploymentService createProxy(Vertx vertx) {
        return new FunctionDeploymentServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(FunctionDeployment.class));
    }

    /**
     * Find all function deployments that belong to a deployment.
     *
     * @param deploymentId the id of the deployment
     * @return a Future that emits all function deployments as JsonArray
     */
    Future<JsonArray> findAllByDeploymentId(long deploymentId);
}
