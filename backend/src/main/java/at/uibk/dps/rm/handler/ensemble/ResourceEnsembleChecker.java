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

    /**
     * Submit the creation of a new resource ensemble by the ensembleId and resourceId.
     *
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a Single that emits the persisted entity
     */
    public Single<JsonObject> submitCreate(long ensembleId, long resourceId) {
        return service.saveByEnsembleIdAndResourceId(ensembleId, resourceId);
    }

    /**
     * Submit the deletion of a resource ensemble by its ensembleId and resourceId
     *
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a Completable
     */
    public Completable submitDelete(long ensembleId, long resourceId) {
        return service.deleteByEnsembleIdAndResourceId(ensembleId, resourceId);
    }

    /**
     * Check if a resource ensemble with the ensembleId and resourceId already exists.
     *
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a Completable if it does not violate uniqueness, else an AlreadyExistsException
     * gets thrown.
     */
    public Completable checkForDuplicateEntity(long ensembleId, long resourceId) {
        Single<Boolean> alreadyExists = service.checkExistsByEnsembleIdAndResourceId(ensembleId, resourceId);
        return ErrorHandler.handleDuplicates(alreadyExists).ignoreElement();
    }

    /**
     * Check if a resource ensemble with the ensembleId and resourceId exists.
     *
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a Completable if it exists, else a NotFoundException gets thrown
     */
    public Completable checkExistsOne(long ensembleId, long resourceId) {
        Single<Boolean> existsOne = service.checkExistsByEnsembleIdAndResourceId(ensembleId, resourceId);
        return ErrorHandler.handleExistsOne(existsOne).ignoreElement();
    }
}
