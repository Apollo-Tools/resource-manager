package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.handler.EntityChecker;
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
@Deprecated
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
     * @param accountId the id of the creator
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a Single that emits the persisted entity
     */
    public Single<JsonObject> submitCreate(long accountId, long ensembleId, long resourceId) {
        return service.saveByEnsembleIdAndResourceId(accountId, ensembleId, resourceId);
    }

    /**
     * Submit the deletion of a resource ensemble by its ensembleId and resourceId
     *
     * @param accountId the id of the creator
     * @param ensembleId the id of the ensemble
     * @param resourceId the id of the resource
     * @return a Completable
     */
    public Completable submitDelete(long accountId, long ensembleId, long resourceId) {
        return service.deleteByEnsembleIdAndResourceId(accountId, ensembleId, resourceId);
    }
}
