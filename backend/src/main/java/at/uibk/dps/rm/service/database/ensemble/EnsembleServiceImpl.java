package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #EnsembleService.
 *
 * @author matthi-g
 */
public class EnsembleServiceImpl extends DatabaseServiceProxy<Ensemble> implements EnsembleService {

    private final EnsembleRepository repository;

    /**
     * Create an instance from the ensembleRepository.
     *
     * @param repository the ensemble repository
     */
    public EnsembleServiceImpl(EnsembleRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, Ensemble.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(repository.findAll())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Ensemble entity: result) {
                    entity.setCreatedBy(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        return Future
            .fromCompletionStage(repository.findAllByAccountId(accountId))
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
            .fromCompletionStage(repository.findByIdAndAccountId(id, accountId))
            .map(result -> {
                if (result != null) {
                    result.setCreatedBy(null);
                }
                return JsonObject.mapFrom(result);
            });
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Ensemble> findOne = getSessionFactory().withSession(session ->
            repository.findById(session, id));
        return Future.fromCompletionStage(findOne)
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
            .fromCompletionStage(repository.findByNameAndAccountId(name, accountId))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Void> updateEnsembleValidity(long ensembleId, boolean isValid) {
        return Future
            .fromCompletionStage(repository.updateValidity(ensembleId, isValid))
            .mapEmpty();
    }
}
