package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ResourceDeployment;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the resource_deployment entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ResourceDeploymentService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ResourceDeploymentService create(ResourceDeploymentRepository repository, Stage.SessionFactory sessionFactory) {
        return new ResourceDeploymentServiceImpl(repository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ResourceDeploymentService createProxy(Vertx vertx) {
        return new ResourceDeploymentServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ResourceDeployment.class));
    }

    /**
     * Update the status of all resource deployments by their deployment.
     *
     * @param deploymentId the id of the deployment
     * @param deploymentStatusValue the new status
     * @return an empty Future
     */
    Future<Void> updateSetStatusByDeploymentId(long deploymentId, DeploymentStatusValue deploymentStatusValue);
}
