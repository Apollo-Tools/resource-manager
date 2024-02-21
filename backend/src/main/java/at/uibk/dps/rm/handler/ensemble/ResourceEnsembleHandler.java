package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.ResourceEnsembleService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.misc.HttpHelper;
import at.uibk.dps.rm.util.validation.SLOValidator;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

/**
 * Processes the http requests that concern the resources linked to the ensemble entity.
 *
 * @author matthi-g
 */
public class ResourceEnsembleHandler extends ValidationHandler {

    private final ResourceEnsembleService resourceEnsembleService;

    private final EnsembleService ensembleService;

    private final ResourceService resourceService;

    private final MetricQueryService metricQueryService;

    /**
     * Create an instance from the resourceEnsembleService.
     *
     * @param resourceEnsembleService the service
     */
    public ResourceEnsembleHandler(ResourceEnsembleService resourceEnsembleService, EnsembleService ensembleService,
            ResourceService resourceService, MetricQueryService metricQueryService) {
        super(resourceEnsembleService);
        this.resourceEnsembleService = resourceEnsembleService;
        this.ensembleService = ensembleService;
        this.resourceService = resourceService;
        this.metricQueryService = metricQueryService;
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return new ConfigUtility(Vertx.currentContext().owner()).getConfigDTO().flatMap(configDTO ->
            HttpHelper.getLongPathParam(rc, "ensembleId")
            .flatMap(ensembleId -> HttpHelper.getLongPathParam(rc, "resourceId")
                .flatMap(resourceId -> ensembleService.findOneByIdAndAccountId(ensembleId, accountId)
                    .flatMap(ensemble -> resourceService.findAllByNonMonitoredSLOs(ensemble)
                        .flatMapObservable(Observable::fromIterable)
                        .filter(resource -> ((JsonObject)resource).getLong("resource_id").equals(resourceId))
                        .map(resource -> ((JsonObject) resource).mapTo(Resource.class))
                        .toList()
                        .flatMap(filteredResources -> {
                            if (filteredResources.isEmpty()) {
                                return Single.error(new BadInputException("resource does not fulfill service level " +
                                    "objectives"));
                            }
                            GetOneEnsemble getOneEnsemble = ensemble.mapTo(GetOneEnsemble.class);
                            SLOValidator sloValidator =
                                new SLOValidator(metricQueryService, configDTO, filteredResources);
                            return sloValidator.filterResourcesByMonitoredMetrics(getOneEnsemble);
                        })
                        .flatMap(result -> {
                            if (result.isEmpty()) {
                                return Single.error(new BadInputException("resource does not fulfill service level " +
                                    "objectives"));
                            }
                            return resourceEnsembleService
                                .saveByEnsembleIdAndResourceId(accountId, ensembleId, resourceId);
                        })
                ))
            ));
    }

    @Override
    protected Completable deleteOne(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "ensembleId")
            .flatMapCompletable(ensembleId -> HttpHelper.getLongPathParam(rc, "resourceId")
                .flatMapCompletable(resourceId -> resourceEnsembleService
                    .deleteByEnsembleIdAndResourceId(accountId, ensembleId, resourceId))
            );
    }
}
