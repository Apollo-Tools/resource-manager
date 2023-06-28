package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.repository.ensemble.ResourceEnsembleRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #ResourceEnsembleService.
 *
 * @author matthi-g
 */
public class ResourceEnsembleServiceImpl  extends DatabaseServiceProxy<ResourceEnsemble> implements
    ResourceEnsembleService {

    private final ResourceEnsembleRepository repository;

    /**
     * Create an instance from the resourceEnsembleRepository.
     *
     * @param repository the resource ensemble repository
     */
    public ResourceEnsembleServiceImpl(ResourceEnsembleRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, ResourceEnsemble.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonObject> saveByEnsembleIdAndResourceId(long ensembleId, long resourceId) {
        Ensemble ensemble = new Ensemble();
        ensemble.setEnsembleId(ensembleId);
        Resource resource = new Resource();
        resource.setResourceId(resourceId);
        ResourceEnsemble resourceEnsemble = new ResourceEnsemble();
        resourceEnsemble.setEnsemble(ensemble);
        resourceEnsemble.setResource(resource);
        CompletionStage<ResourceEnsemble> create = withTransaction(session ->
            repository.create(session, resourceEnsemble));
        return Future.fromCompletionStage(create)
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<Void> deleteByEnsembleIdAndResourceId(long ensembleId, long resourceId) {
        CompletionStage<Integer> delete = withTransaction(session ->
            repository.deleteEnsembleIdAndResourceId(session, ensembleId, resourceId));
        return Future.fromCompletionStage(delete)
            .mapEmpty();
    }

    @Override
    public Future<Boolean> checkExistsByEnsembleIdAndResourceId(long ensembleId, long resourceId) {
        CompletionStage<ResourceEnsemble> findOne = withSession(session ->
            repository.findByEnsembleIdAndResourceId(session, ensembleId, resourceId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }
}
