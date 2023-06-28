package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

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
    public EnsembleSLOServiceImpl(EnsembleSLORepository ensembleSLORepository, Stage.SessionFactory sessionFactory) {
        super(ensembleSLORepository, EnsembleSLO.class, sessionFactory);
        this.ensembleSLORepository = ensembleSLORepository;
    }

    @Override
    public Future<JsonArray> findAllByEnsembleId(long ensembleId) {
        CompletionStage<List<EnsembleSLO>> findAll = withSession(session ->
            ensembleSLORepository.findAllByEnsembleId(session, ensembleId));
        return Future.fromCompletionStage(findAll)
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
