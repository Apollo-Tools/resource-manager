package at.uibk.dps.rm.router.ensemble;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.handler.ensemble.ResourceEnsembleHandler;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the ensemble resources route.
 *
 * @author matthi-g
 */
public class ResourceEnsembleRoute implements Route {

    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        ResourceEnsembleHandler ensembleResourceHandler = new ResourceEnsembleHandler(serviceProxyProvider
            .getResourceEnsembleService());
        ResultHandler resultHandler = new ResultHandler(ensembleResourceHandler);

        router
            .operation("addResourceToEnsemble")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("deleteResourceFromEnsemble")
            .handler(resultHandler::handleDeleteRequest);
    }
}
