package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
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
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
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
        return new ConfigUtility(Vertx.currentContext().owner()).getConfigDTO()
            .flatMap(configDTO -> HttpHelper.getLongPathParam(rc, "id")
                .flatMap(id -> validateEnsembleStatus(accountId, id, configDTO))
            );
    }

    /**
     * Validate all existing ensembles.
     *
     * @return A Completable
     */
    public Completable validateAllExistingEnsembles() {
        return new ConfigUtility(Vertx.currentContext().owner()).getConfigDTO()
            .flatMapCompletable(configDTO -> ensembleService.findAll()
                .flatMapObservable(Observable::fromIterable)
                .map(ensemble -> (JsonObject) ensemble)
                .flatMapCompletable(ensemble -> {
                    long ensembleId = ensemble.getLong("ensemble_id");
                    long accountId = ensemble.getJsonObject("created_by").getLong("account_id");
                    return validateEnsembleStatus(accountId, ensembleId, configDTO)
                        .ignoreElement();
                })
            );
    }

    private Single<JsonArray> validateEnsembleStatus(long accountId, long ensembleId, ConfigDTO configDTO) {
        return ensembleService.findOneByIdAndAccountId(ensembleId, accountId)
            .flatMap(ensemble -> {
                GetOneEnsemble getOneEnsemble = ensemble.mapTo(GetOneEnsemble.class);
                return resourceService.findAllByNonMonitoredSLOs(ensemble)
                    .flatMap(resources -> {
                        SLOValidator sloValidator = new SLOValidator(metricQueryService, getOneEnsemble, configDTO);
                        return sloValidator.filterResourcesByMonitoredMetrics(resources);
                    })
                    .map(ArrayList::new)
                    .map(validResources -> {
                        List<ResourceEnsembleStatus> statusValues = EnsembleUtility
                            .getResourceEnsembleStatus(validResources, getOneEnsemble.getResources());
                        return new JsonArray(Json.encode(statusValues));
                    })
                    .flatMap(statusValues -> ensembleService
                        .updateEnsembleStatus(ensembleId, statusValues)
                        .andThen(Single.defer(() -> Single.just(statusValues)))
                    );
            });
    }
}
