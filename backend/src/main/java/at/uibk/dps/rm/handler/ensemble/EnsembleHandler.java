package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.database.util.EnsembleUtility;
import at.uibk.dps.rm.service.rxjava3.database.ensemble.EnsembleService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import at.uibk.dps.rm.util.misc.HttpHelper;
import at.uibk.dps.rm.util.validation.SLOValidator;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.stream.Collectors;

/**
 * Processes the http requests that concern the ensemble entity.
 *
 * @author matthi-g
 */
public class EnsembleHandler extends ValidationHandler {

    private final EnsembleService ensembleService;

    private final ResourceService resourceService;

    private final MetricQueryService metricQueryService;

    /**
     * Create an instance from the ensembleService.
     *
     * @param ensembleService the service
     */
    public EnsembleHandler(EnsembleService ensembleService, ResourceService resourceService,
            MetricQueryService metricQueryService) {
        super(ensembleService);
        this.ensembleService = ensembleService;
        this.resourceService = resourceService;
        this.metricQueryService = metricQueryService;
    }

    @Override
    public Single<JsonObject> getOneFromAccount(RoutingContext rc) {
        return super.getOneFromAccount(rc);
    }

    /**
     * Validate the resources for a create ensemble request.
     *
     * @param rc the routing context
     */
    public Completable validateNewResourceEnsembleSLOs(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        SLORequest requestDTO = requestBody.mapTo(CreateEnsembleRequest.class);
        return new ConfigUtility(Vertx.currentContext().owner()).getConfigDTO()
            .flatMap(configDTO -> resourceService.findAllByNonMonitoredSLOs(requestBody)
                .flatMap(resources -> {
                    SLOValidator sloValidator = new SLOValidator(metricQueryService, requestDTO, configDTO);
                    return sloValidator.filterResourcesByMonitoredMetrics(resources);
                }))
            .flatMapObservable(Observable::fromIterable)
            .map(Resource::getResourceId)
            .collect(Collectors.toSet())
            .flatMapCompletable(filteredResourceIds -> ensembleService.validateCreateEnsembleRequest(requestBody,
                filteredResourceIds));
    }

    /**
     * Validate an existing ensemble.
     *
     * @param rc the routing context
     * @return a Single that emits all resources with their validity state
     */
    public Single<JsonArray> validateExistingEnsemble(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
        .flatMap(id -> {
            ensembleService.findOneByIdAndAccountId(accountId, id)
                .flatMap(ensemble -> {
                    GetOneEnsemble getOneEnsemble = ensemble.mapTo(GetOneEnsemble.class);
                    return resourceService.findAllByNonMonitoredSLOs(ensemble)
                        .flatMapObservable(Observable::fromIterable)
                        .map(resource -> ((JsonObject) resource).mapTo(Resource.class))
                        .toList()
                        .map(validResources -> EnsembleUtility.getResourceEnsembleStatus(validResources,
                            getOneEnsemble.getResources()));
                });



            return ensembleService.validateExistingEnsemble(accountId, id);
        });
    }

    /**
     * Validate all existing ensembles.
     *
     * @return A Completable
     */
    public Completable validateAllExistingEnsembles() {
        return ensembleService.validateAllExistingEnsembles();
    }
}
