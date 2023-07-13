package at.uibk.dps.rm.service.database.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.deployment.DeploymentRepository;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentRepository;
import at.uibk.dps.rm.repository.deployment.ResourceDeploymentStatusRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #DeploymentService.
 *
 * @author matthi-g
 */
public class DeploymentServiceImpl extends DatabaseServiceProxy<Deployment> implements DeploymentService {

    private final DeploymentRepository repository;

    private final ResourceDeploymentRepository resourceDeploymentRepository;

    private final ResourceDeploymentStatusRepository statusRepository;

    /**
     * Create an instance from the deploymentRepository.
     *
     * @param repository the deployment repository
     */
    public DeploymentServiceImpl(DeploymentRepository repository,
            ResourceDeploymentRepository resourceDeploymentRepository,
            ResourceDeploymentStatusRepository statusRepository, Stage.SessionFactory sessionFactory) {
        super(repository, Deployment.class, sessionFactory);
        this.repository = repository;
        this.resourceDeploymentRepository = resourceDeploymentRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    public Future<JsonObject> cancelDeployment(long id, long accountId) {
        CompletionStage<Deployment> update = withTransaction(session ->
            repository.findByIdAndAccountId(session, id, accountId)
                .thenCompose(deployment -> {
                    ServiceResultValidator.checkFound(deployment, Deployment.class);
                    return resourceDeploymentRepository.findAllByDeploymentIdAndFetch(session, id)
                        .thenCompose(resourceDeployments -> {
                            long deployedAmount = resourceDeployments.stream().filter(resourceDeployment ->
                                DeploymentStatusValue.fromDeploymentStatus(resourceDeployment.getStatus())
                                    .equals(DeploymentStatusValue.DEPLOYED))
                                .count();
                            if (resourceDeployments.isEmpty() || deployedAmount != resourceDeployments.size()) {
                                throw new BadInputException("invalid deployment state");
                            }
                            return statusRepository.findOneByStatusValue(session,
                                    DeploymentStatusValue.TERMINATING.getValue())
                                .thenAccept(status -> resourceDeployments
                                    .forEach(resourceDeployment -> resourceDeployment.setStatus(status)));
                        })
                        .thenApply(res -> deployment);
                })
        );
        return transactionToFuture(update).map(deployment -> {
            deployment.setCreatedBy(null);
            return JsonObject.mapFrom(deployment);
        });
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        CompletionStage<List<Deployment>> findAll = withSession(session ->
            repository.findAllByAccountId(session, accountId));
        return Future.fromCompletionStage(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Deployment entity: result) {
                    entity.setCreatedBy(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonObject> findOneByIdAndAccountId(long id, long accountId) {
        CompletionStage<Deployment> findOne = withSession(session ->
            repository.findByIdAndAccountId(session, id, accountId));
        return Future.fromCompletionStage(findOne)
            .map(result -> {
                if (result != null) {
                    result.setCreatedBy(null);
                }
                return JsonObject.mapFrom(result);
            });
    }
}
