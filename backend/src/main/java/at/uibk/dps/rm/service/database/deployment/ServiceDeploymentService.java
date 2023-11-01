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
import io.vertx.core.json.JsonObject;

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
     * Find a service deployment that is ready for startup or termination by its id and creator.
     *
     * @param resourceDeploymentId the id of the service deployment
     * @param accountId the account id of the creator
     * @param resultHandler receives the service deployment if it exists and is ready for startup and termination
     *                      else an error
     */
    void findOneForDeploymentAndTermination(long resourceDeploymentId, long accountId,
        Handler<AsyncResult<JsonObject>> resultHandler);
}
