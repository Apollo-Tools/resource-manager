package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.repository.deployment.DeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the deployment entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface DeploymentService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static DeploymentService create(DeploymentRepository repository, Stage.SessionFactory sessionFactory) {
        return new DeploymentServiceImpl(repository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static DeploymentService createProxy(Vertx vertx) {
        return new DeploymentServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Deployment.class));
    }

    /**
     * Find all deployments by their creator account.
     *
     * @param accountId the id of the account
     * @return a Future that emits all deployments as JsonArray
     */
    Future<JsonArray> findAllByAccountId(long accountId);

    /**
     * Find one deployment by its id and creator account.
     *
     * @param id the id of the deployment
     * @param accountId the id of the creator account
     * @return a Future that emits the deployment as JsonObject if it exists, else null
     */
    Future<JsonObject> findOneByIdAndAccountId(long id, long accountId);
}
