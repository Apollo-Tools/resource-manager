package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.ResourceEnsembleService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

public class ResourceEnsembleChecker extends EntityChecker {

    private final ResourceEnsembleService service;
    /**
     * Create an instance from the service.
     *
     * @param service the resource ensemble service to use
     */
    public ResourceEnsembleChecker(ResourceEnsembleService service) {
        super(service);
        this.service = service;
    }

    public Single<JsonArray> checkFindAllByEnsemble(long ensembleId) {
        Single<JsonArray> findAllByEnsemble = service.findAllByEnsembleId(ensembleId);
        return ErrorHandler.handleFindAll(findAllByEnsemble);
    }
}
