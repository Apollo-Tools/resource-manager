package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.deployment.FunctionDeploymentService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

/**
 * Implements methods to perform CRUD operations on the function_deployment entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class FunctionDeploymentChecker extends EntityChecker {

    private final FunctionDeploymentService service;

    /**
     * Create an instance from the service.
     *
     * @param service the function deployment service
     */
    public FunctionDeploymentChecker(FunctionDeploymentService service) {
        super(service);
        this.service = service;
    }

    /**
     * Find all function deployments by deployment.
     *
     * @return a Single that emits all found resource deployments as JsonArray
     */
    public Single<JsonArray> checkFindAllByDeploymentId(long id) {
        final Single<JsonArray> findAllByDeploymentId = service.findAllByDeploymentId(id);
        return ErrorHandler.handleFindAll(findAllByDeploymentId);
    }
}
