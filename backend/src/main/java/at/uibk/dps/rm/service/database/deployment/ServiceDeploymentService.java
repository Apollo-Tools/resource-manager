package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.vertx.core.json.JsonArray;

import java.util.List;

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
    static ServiceDeploymentService create(ServiceDeploymentRepository repository, SessionManagerProvider smProvider) {
        return new ServiceDeploymentServiceImpl(repository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ServiceDeploymentService createProxy(Vertx vertx) {
        return new ServiceDeploymentServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ServiceDeployment.class));
    }

    /**
     * Find all service deployments that are ready for startup or termination by their id and creator.
     *
     * @param resourceDeploymentIds the ids of the service deployments
     * @param accountId the account id of the creator
     * @param ignoreRunningStateChange ignore state change that has been started previously and not
     *                                finished yet
     * @param resultHandler receives the list of service deployments
     */
    void findAllForStartupAndShutdown(List<Long> resourceDeploymentIds, long accountId, long deploymentId,
        boolean ignoreRunningStateChange, Handler<AsyncResult<JsonArray>> resultHandler);
}
