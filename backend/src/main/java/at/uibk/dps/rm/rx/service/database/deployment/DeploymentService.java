package at.uibk.dps.rm.rx.service.database.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.rx.repository.DeploymentRepositoryProvider;
import at.uibk.dps.rm.rx.service.ServiceProxyAddress;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
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
    static DeploymentService create(Stage.SessionFactory sessionFactory) {
        return new DeploymentServiceImpl(new DeploymentRepositoryProvider(), sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static DeploymentService createProxy(Vertx vertx) {
        return new DeploymentServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Deployment.class));
    }

    /**
     * Cancel a deployment by setting the status to "TERMINATING"
     *
     * @param id the id of the deployment
     * @param accountId the id of the creator account
     * @param resultHandler receives nothing if the cancellation was successful else an error
     */
    void cancelDeployment(long id, long accountId, Handler<AsyncResult<JsonObject>> resultHandler);

    /**
     * Handle a deployment error.
     *
     * @param id the id of the deployment
     * @param errorMessage the error message of the error
     * @param resultHandler receives nothing if the error was handled successfully else an error
     */
    void handleDeploymentError(long id, String errorMessage, Handler<AsyncResult<Void>> resultHandler);

    /**
     * Handle a successful deployment.
     *
     * @param terraformOutput the output of the deployment process
     * @param request the deployment request
     * @param resultHandler receives nothing if the update was successful else an error
     */
    void handleDeploymentSuccessful(JsonObject terraformOutput, DeployResourcesDTO request,
        Handler<AsyncResult<Void>> resultHandler);
}
