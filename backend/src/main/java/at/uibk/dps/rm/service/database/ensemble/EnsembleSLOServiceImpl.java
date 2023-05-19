package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

/**
 * This is the implementation of the #EnsembleSLOService.
 *
 * @author matthi-g
 */
public class EnsembleSLOServiceImpl extends DatabaseServiceProxy<EnsembleSLO> implements EnsembleSLOService {
    private final EnsembleSLORepository ensembleSLORepository;

    /**
     * Create an instance from the ensembleSLORepository.
     *
     * @param ensembleSLORepository the ensemble slo repository
     */
    public EnsembleSLOServiceImpl(EnsembleSLORepository ensembleSLORepository) {
        super(ensembleSLORepository, EnsembleSLO.class);
        this.ensembleSLORepository = ensembleSLORepository;
    }

    @Override
    public Future<JsonArray> findAllByEnsembleId(long ensembleId) {
        return Future
            .fromCompletionStage(ensembleSLORepository.findAllByEnsembleId(ensembleId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (EnsembleSLO entity : result) {
                    entity.setEnsemble(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }
}
