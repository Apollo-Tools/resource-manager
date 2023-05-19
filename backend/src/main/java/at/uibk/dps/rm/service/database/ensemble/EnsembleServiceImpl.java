package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This is the implementation of the #EnsembleService.
 *
 * @author matthi-g
 */
public class EnsembleServiceImpl extends DatabaseServiceProxy<Ensemble> implements EnsembleService {

    private final EnsembleRepository ensembleRepository;

    /**
     * Create an instance from the ensembleRepository.
     *
     * @param ensembleRepository the ensemble repository
     */
    public EnsembleServiceImpl(EnsembleRepository ensembleRepository) {
        super(ensembleRepository, Ensemble.class);
        this.ensembleRepository = ensembleRepository;
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        return Future
            .fromCompletionStage(ensembleRepository.findAllByAccountId(accountId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Ensemble entity: result) {
                    entity.setCreatedBy(null);
                    entity.setResource_types(null);
                    entity.setRegions(null);
                    entity.setProviders(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonObject> findOneByIdAndAccountId(long id, long accountId) {
        return Future
            .fromCompletionStage(ensembleRepository.findByIdAndAccountId(id, accountId))
            .map(result -> {
                if (result != null) {
                    result.setCreatedBy(null);
                }
                return JsonObject.mapFrom(result);
            });
    }

    @Override
    public Future<Boolean> existsOneByNameAndAccountId(String name, long accountId) {
        return Future
            .fromCompletionStage(ensembleRepository.findByNameAndAccountId(name, accountId))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Void> updateEnsembleValidity(long ensembleId, boolean isValid) {
        return Future
            .fromCompletionStage(ensembleRepository.updateValidity(ensembleId, isValid))
            .mapEmpty();
    }
}
