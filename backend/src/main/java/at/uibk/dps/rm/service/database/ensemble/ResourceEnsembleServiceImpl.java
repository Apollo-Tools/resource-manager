package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.repository.ensemble.ResourceEnsembleRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class ResourceEnsembleServiceImpl  extends DatabaseServiceProxy<ResourceEnsemble> implements
    ResourceEnsembleService {

    private final ResourceEnsembleRepository repository;

    /**
     * Create an instance from the resourceEnsembleRepository.
     *
     * @param repository the resource ensemble repository
     */
    public ResourceEnsembleServiceImpl(ResourceEnsembleRepository repository) {
        super(repository, ResourceEnsemble.class);
        this.repository = repository;
    }

    public Future<JsonArray> findAllByEnsembleId(long ensembleId) {
        return Future
            .fromCompletionStage(repository.findAllByEnsembleId(ensembleId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (ResourceEnsemble entity: result) {
                    entity.setEnsemble(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }
}
