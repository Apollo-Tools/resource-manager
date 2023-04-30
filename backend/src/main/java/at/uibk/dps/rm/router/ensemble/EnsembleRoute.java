package at.uibk.dps.rm.router.ensemble;

import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.ensemble.*;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

public class EnsembleRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        EnsembleChecker ensembleChecker = new EnsembleChecker(serviceProxyProvider.getEnsembleService());
        EnsembleSLOChecker ensembleSLOChecker = new EnsembleSLOChecker(serviceProxyProvider.getEnsembleSLOService());
        ResourceEnsembleChecker resourceEnsembleChecker = new ResourceEnsembleChecker(serviceProxyProvider
            .getResourceEnsembleService());
        ResourceChecker resourceChecker = new ResourceChecker(serviceProxyProvider.getResourceService());
        EnsembleHandler ensembleHandler = new EnsembleHandler(ensembleChecker, ensembleSLOChecker,
            resourceEnsembleChecker, resourceChecker);
        ResultHandler resultHandler = new ResultHandler(ensembleHandler);

        router
            .operation("createEnsemble")
            .handler(SLOInputHandler::validateCreateEnsembleRequest)
            .handler(resultHandler::handleSaveOneRequest);

        router
            .operation("listEnsembles")
            .handler(resultHandler::handleFindAllRequest);

        router
            .operation("getEnsemble")
            .handler(resultHandler::handleFindOneRequest);

        router
            .operation("deleteEnsemble")
            .handler(resultHandler::handleDeleteRequest);
    }
}
