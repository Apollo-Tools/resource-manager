package at.uibk.dps.rm.router.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.handler.ResultHandler;
import at.uibk.dps.rm.handler.ensemble.*;
import at.uibk.dps.rm.handler.metric.MetricChecker;
import at.uibk.dps.rm.handler.metric.MetricValueChecker;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.handler.resource.ResourceSLOHandler;
import at.uibk.dps.rm.router.Route;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.rxjava3.ext.web.openapi.RouterBuilder;

/**
 * Used to initialise the ensemble route.
 *
 * @author matthi-g
 */
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
        MetricChecker metricChecker = new MetricChecker(serviceProxyProvider.getMetricService());
        MetricValueChecker metricValueChecker = new MetricValueChecker(serviceProxyProvider.getMetricValueService());
        ResourceSLOHandler resourceSLOHandler = new ResourceSLOHandler(resourceChecker, metricChecker,
            metricValueChecker);
        ResultHandler resultHandler = new ResultHandler(ensembleHandler);

        router
            .operation("createEnsemble")
            .handler(EnsembleInputHandler::validateCreateEnsembleRequest)
            .handler(rc -> {
                CreateEnsembleRequest requestDTO = rc.body()
                        .asJsonObject()
                        .mapTo(CreateEnsembleRequest.class);
                resourceSLOHandler.validateNewResourceEnsembleSLOs(rc, requestDTO);
            })
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
            .handler(rc -> ensembleHandler.getOne(rc)
                .flatMap(ensembleJson-> {
                    GetOneEnsemble ensemble = ensembleJson.mapTo(GetOneEnsemble.class);
                    return resourceSLOHandler.validateExistingEnsemble(ensemble)
                        .flatMap(resourceEnsembleStatuses -> Observable.fromIterable(resourceEnsembleStatuses)
                            .all(ResourceEnsembleStatus::getIsValid)
                            .flatMapCompletable(allValid -> ensembleChecker
                                .submitUpdateValidity(ensemble.getEnsembleId(), allValid))
                            .andThen(Single.defer(() -> Single.just(resourceEnsembleStatuses))));
                })
                .subscribe(res -> rc.end(new JsonArray(res).encodePrettily()).subscribe(),
                    throwable -> ResultHandler.handleRequestError(rc, throwable)));
    }
}
