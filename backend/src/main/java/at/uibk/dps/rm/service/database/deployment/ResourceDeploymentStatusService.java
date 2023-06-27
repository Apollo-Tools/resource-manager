package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ResourceDeploymentStatus;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentStatusRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the resource_deployment_status entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ResourceDeploymentStatusService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ResourceDeploymentStatusService create(ResourceDeploymentStatusRepository repository,
            Stage.SessionFactory sessionFactory) {
        return new ResourceDeploymentStatusServiceImpl(repository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ResourceDeploymentStatusService createProxy(Vertx vertx) {
        return new ResourceDeploymentStatusServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ResourceDeploymentStatus.class));
    }

    /**
     * Find a resource deployment status by its value.
     *
     * @param statusValue the value of the status
     * @return a Future that emits the resource deployment status as JsonObject if it exists, else
     * null
     */
    Future<JsonObject> findOneByStatusValue(String statusValue);
}
