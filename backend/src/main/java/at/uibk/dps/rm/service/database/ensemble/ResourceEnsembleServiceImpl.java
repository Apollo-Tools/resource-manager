package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.repository.ensemble.ResourceEnsembleRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.ServiceLevelObjectiveMapper;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * This is the implementation of the #ResourceEnsembleService.
 *
 * @author matthi-g
 */
public class ResourceEnsembleServiceImpl  extends DatabaseServiceProxy<ResourceEnsemble> implements
    ResourceEnsembleService {

    private final ResourceEnsembleRepository repository;

    private final EnsembleSLORepository ensembleSLORepository;

    private final EnsembleRepository ensembleRepository;

    private final ResourceRepository resourceRepository;

    /**
     * Create an instance from the resourceEnsembleRepository, ensembleSLORepository,
     * ensembleRepository, resourceRepository.
     *
     * @param repository the resource ensemble repository
     * @param ensembleSLORepository the ensemble slo repository
     * @param ensembleRepository  the ensemble repository
     * @param resourceRepository  the resource repository
     */
    public ResourceEnsembleServiceImpl(ResourceEnsembleRepository repository,
            EnsembleSLORepository ensembleSLORepository, EnsembleRepository ensembleRepository,
            ResourceRepository resourceRepository, Stage.SessionFactory sessionFactory) {
        super(repository, ResourceEnsemble.class, sessionFactory);
        this.repository = repository;
        this.ensembleSLORepository = ensembleSLORepository;
        this.ensembleRepository = ensembleRepository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public Future<JsonObject> saveByEnsembleIdAndResourceId(long accountId, long ensembleId, long resourceId) {
        ResourceEnsemble resourceEnsemble = new ResourceEnsemble();
        CompletionStage<ResourceEnsemble> create = withTransaction(session ->
            ensembleRepository.findByIdAndAccountId(session, ensembleId, accountId)
                .thenCompose(ensemble -> {
                    ServiceResultValidator.checkFound(ensemble, Ensemble.class);
                    resourceEnsemble.setEnsemble(ensemble);
                    return resourceRepository.findByIdAndFetch(session, resourceId);
                })
                .thenCompose(resource -> {
                    ServiceResultValidator.checkFound(resource, Resource.class);
                    resourceEnsemble.setResource(resource);
                    return repository.findByEnsembleIdAndResourceId(session, ensembleId, resourceId);
                })
                .thenCompose(existingResourceEnsemble -> {
                    ServiceResultValidator.checkExists(existingResourceEnsemble, ResourceEnsemble.class);
                    return ensembleSLORepository.findAllByEnsembleId(session, ensembleId);
                })
                .thenApply(ensembleSLOS -> ensembleSLOS.stream()
                    .map(ServiceLevelObjectiveMapper::mapEnsembleSLO)
                    .collect(Collectors.toList())
                )
                .thenApply(slos -> {
                    boolean isValidByMetrics = SLOCompareUtility
                        .resourceFilterBySLOValueType(resourceEnsemble.getResource(), slos);
                    boolean isValidByNonMetrics = SLOCompareUtility
                        .resourceValidByNonMetricSLOS(resourceEnsemble.getResource(), resourceEnsemble.getEnsemble());
                    if (!isValidByMetrics || !isValidByNonMetrics) {
                        throw new BadInputException("resource does not fulfill service level objectives");
                    }
                    session.persist(resourceEnsemble);
                    return resourceEnsemble;
                })
        );
        return Future.fromCompletionStage(create)
            .recover(this::recoverFailure)
            .map(result -> {
                JsonObject response = new JsonObject();
                response.put("ensemble_id", result.getEnsemble().getEnsembleId());
                response.put("resource_id", result.getResource().getResourceId());
                return response;
            });
    }

    @Override
    public Future<Void> deleteByEnsembleIdAndResourceId(long accountId, long ensembleId, long resourceId) {
        CompletionStage<Void> delete = withTransaction(session ->
            repository.findByEnsembleIdAndResourceId(session, ensembleId, resourceId)
                .thenAccept(resourceEnsemble -> {
                    ServiceResultValidator.checkFound(resourceEnsemble, ResourceEnsemble.class);
                    session.remove(resourceEnsemble);
                })
        );
        return Future.fromCompletionStage(delete)
            .recover(this::recoverFailure);
    }

    @Override
    public Future<Boolean> checkExistsByEnsembleIdAndResourceId(long ensembleId, long resourceId) {
        CompletionStage<ResourceEnsemble> findOne = withSession(session ->
            repository.findByEnsembleIdAndResourceId(session, ensembleId, resourceId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }
}
