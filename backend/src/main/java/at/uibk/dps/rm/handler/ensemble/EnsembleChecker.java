package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class EnsembleChecker extends EntityChecker {

    private final EnsembleService ensembleService;
    /**
     * Create an instance from the ensembleService.
     *
     * @param ensembleService the ensemble service to use
     */
    public EnsembleChecker(EnsembleService ensembleService) {
        super(ensembleService);
        this.ensembleService = ensembleService;
    }

    public Single<JsonObject> checkFindOne(long id, long accountId) {
        Single<JsonObject> findOneById = ensembleService.findOneByIdAndAccountId(id, accountId);
        return ErrorHandler.handleFindOne(findOneById);
    }

    public Completable checkExistsOneByName(String name, long accountId) {
        Single<Boolean> existsOneById = ensembleService.existsOneByNameAndAccountId(name, accountId);
        return ErrorHandler.handleDuplicates(existsOneById).ignoreElement();
    }

    public Single<JsonArray> checkFindAll(long accountId) {
        return ErrorHandler.handleFindAll(ensembleService.findAllByAccountId(accountId));
    }
}
