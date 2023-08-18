package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.repository.deployment.ServiceDeploymentRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import org.hibernate.reactive.stage.Stage;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #ServiceDeploymentService.
 *
 * @author matthi-g
 */
public class ServiceDeploymentServiceImpl extends DatabaseServiceProxy<ServiceDeployment>
    implements ServiceDeploymentService {

    private final ServiceDeploymentRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the service deployment repository
     */
    public ServiceDeploymentServiceImpl(ServiceDeploymentRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, ServiceDeployment.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<Boolean> existsReadyForContainerStartupAndTermination(long deploymentId,
            long resourceDeploymentId, long accountId) {
        CompletionStage<ServiceDeployment> findOne = withSession(session -> repository.findOneByDeploymentStatus(
            session, deploymentId, resourceDeploymentId, accountId, DeploymentStatusValue.DEPLOYED));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }
}
