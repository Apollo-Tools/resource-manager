package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.dto.DeployResourcesRequest;
import at.uibk.dps.rm.repository.account.CredentialsRepository;
import at.uibk.dps.rm.repository.deployment.DeploymentRepository;
import at.uibk.dps.rm.repository.function.FunctionRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.repository.service.ServiceRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the deployment entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface DeploymentPreconditionService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static DeploymentPreconditionService create(Stage.SessionFactory sessionFactory) {
        return new DeploymentPreconditionServiceImpl(new DeploymentRepository(), new FunctionRepository(),
            new ServiceRepository(), new ResourceRepository(), new PlatformMetricRepository(), new VPCRepository(),
            new CredentialsRepository(), sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static DeploymentPreconditionService createProxy(Vertx vertx) {
        return new DeploymentPreconditionServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(
            "deployment-precondition"));
    }

    Future<JsonArray> checkDeploymentIsValid(long accountId, DeployResourcesRequest requestDTO);
}
