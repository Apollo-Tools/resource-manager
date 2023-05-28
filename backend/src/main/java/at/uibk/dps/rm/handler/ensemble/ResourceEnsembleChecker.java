package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.ResourceEnsembleService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the resource_ensemble entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
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

    public Single<JsonObject> submitCreate(long ensembleId, long resourceId) {
        return service.saveByEnsembleIdAndResourceId(ensembleId, resourceId);
    }

    public Completable submitDelete(long ensembleId, long resourceId) {
        return service.deleteByEnsembleIdAndResourceId(ensembleId, resourceId);
    }

    public Completable checkForDuplicateEntity(long ensembleId, long resourceId) {
        Single<Boolean> alreadyExists = service.checkExistsByEnsembleIdAndResourceId(ensembleId, resourceId);
        return ErrorHandler.handleDuplicates(alreadyExists).ignoreElement();
    }

    public Completable checkExistsOne(long ensembleId, long resourceId) {
        Single<Boolean> existsOne = service.checkExistsByEnsembleIdAndResourceId(ensembleId, resourceId);
        return ErrorHandler.handleExistsOne(existsOne).ignoreElement();
    }
}
