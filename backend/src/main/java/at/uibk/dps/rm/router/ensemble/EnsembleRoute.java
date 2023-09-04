package at.uibk.dps.rm.router.ensemble;

import at.uibk.dps.rm.rx.handler.PrivateEntityResultHandler;
import at.uibk.dps.rm.rx.handler.ResultHandler;
import at.uibk.dps.rm.rx.handler.ensemble.*;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the ensemble route.
 *
 * @author matthi-g
 */
public class EnsembleRoute implements Route {
    @Override
    public void init(RouterBuilder router, ServiceProxyProvider serviceProxyProvider) {
        EnsembleHandler ensembleHandler = new EnsembleHandler(serviceProxyProvider.getEnsembleService());
        PrivateEntityResultHandler resultHandler = new PrivateEntityResultHandler(ensembleHandler);

        router
            .operation("createEnsemble")
            .handler(EnsembleInputHandler::validateCreateEnsembleRequest)
            .handler(rc -> ensembleHandler.validateNewResourceEnsembleSLOs(rc)
                .subscribe(rc::next, throwable -> ResultHandler.handleRequestError(rc, throwable)))
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

        router
            .operation("validateEnsemble")
            .handler(rc -> ensembleHandler.validateExistingEnsemble(rc)
                .subscribe(res -> rc.end(res.encodePrettily()).subscribe(),
                    throwable -> ResultHandler.handleRequestError(rc, throwable))
            );

        Vertx vertx = Vertx.currentContext().owner();
        new ConfigUtility(vertx).getConfigDTO()
            .map(config -> {
                long period = Double.valueOf(config.getEnsembleValidationPeriod() * 60 * 1000).longValue();
                return vertx.setPeriodic(period, id -> ensembleHandler.validateAllExistingEnsembles().subscribe());
            })
            .subscribe();
    }
}
