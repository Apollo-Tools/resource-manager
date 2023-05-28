package at.uibk.dps.rm.router.ensemble;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.ensemble.*;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.router.Route;
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
        ResourceEnsembleChecker resourceEnsembleChecker = new ResourceEnsembleChecker(serviceProxyProvider
            .getResourceEnsembleService());
        EnsembleChecker ensembleChecker = new EnsembleChecker(serviceProxyProvider.getEnsembleService());
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        EnsembleSLOChecker ensembleSLOChecker = new EnsembleSLOChecker(serviceProxyProvider.getEnsembleSLOService());
        ResourceEnsembleHandler ensembleResourceHandler = new ResourceEnsembleHandler(resourceEnsembleChecker,
            ensembleChecker, ensembleSLOChecker , resourceChecker);
        ResultHandler resultHandler = new ResultHandler(ensembleResourceHandler);

        router
            .operation("addResourceToEnsemble")
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("deleteResourceFromEnsemble")
            .handler(resultHandler::handleDeleteRequest);

    }
}
