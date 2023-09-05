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
import org.hibernate.reactive.stage.Stage;

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
    static ServiceDeploymentService create(ServiceDeploymentRepository repository, Stage.SessionFactory sessionFactory) {
        return new ServiceDeploymentServiceImpl(repository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ServiceDeploymentService createProxy(Vertx vertx) {
        return new ServiceDeploymentServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ServiceDeployment.class));
    }

    /**
     * Check if a service deployment is ready for startup or termination
     *
     * @param deploymentId the id of the deployment
     * @param resourceDeploymentId the id of the resource deployment
     * @param accountId the account id of the creator
     * @param resultHandler receives true if the container is ready for startup and termination
     *                      else false
     */
    void existsReadyForContainerStartupAndTermination(long deploymentId, long resourceDeploymentId, long accountId,
        Handler<AsyncResult<Boolean>> resultHandler);
}
