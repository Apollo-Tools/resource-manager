package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.List;
import java.util.stream.Collectors;

public class EnsembleHandler extends ValidationHandler {

    private final EnsembleChecker ensembleChecker;

    private final EnsembleSLOChecker ensembleSLOChecker;

    private final ResourceEnsembleChecker resourceEnsembleChecker;

    private final ResourceChecker resourceChecker;

    /**
     * Create an instance from the ensembleChecker.
     *
     * @param ensembleChecker the ensemble checker
     */
    public EnsembleHandler(EnsembleChecker ensembleChecker, EnsembleSLOChecker ensembleSLOChecker,
                           ResourceEnsembleChecker resourceEnsembleChecker, ResourceChecker resourceChecker) {
        super(ensembleChecker);
        this.ensembleChecker = ensembleChecker;
        this.ensembleSLOChecker = ensembleSLOChecker;
        this.resourceEnsembleChecker = resourceEnsembleChecker;
        this.resourceChecker = resourceChecker;
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        CreateEnsembleRequest request = rc.body().asJsonObject().mapTo(CreateEnsembleRequest.class);
        long accountId = rc.user().principal().getLong("account_id");
        Ensemble ensemble = createNewEnsemble(request, accountId);
        return ensembleChecker.checkExistsOneByName(ensemble.getName(), accountId)
            .andThen(Observable.fromIterable(request.getResources())
                .flatMap(resourceId -> resourceChecker.checkExistsOne(resourceId.getResourceId())
                    .andThen(Observable.defer(() -> Observable.just(resourceId)))))
                .toList()
            .flatMap(result -> entityChecker.submitCreate(JsonObject.mapFrom(ensemble)))
            .flatMap(result -> {
                Ensemble persistedEnsemble = result.mapTo(Ensemble.class);
                return createResourceEnsembles(persistedEnsemble, request.getResources())
                    .andThen(createEnsembleSLOs(persistedEnsemble, request.getServiceLevelObjectives()))
                    .andThen(Single.defer(() -> {
                        JsonObject returnObject = result.copy();
                        returnObject.remove("slos");
                        returnObject.remove("regions");
                        returnObject.remove("providers");
                        returnObject.remove("resource_types");
                        returnObject.remove("created_by");
                        return Single.just(returnObject);
                    }));
            });
    }

    @Override
    protected Single<JsonObject> getOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> ensembleChecker.checkFindOne(id, rc.user().principal().getLong("account_id")));
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return ensembleChecker.checkFindAll(accountId);
    }

    @Override
    protected Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> ensembleChecker.checkFindOne(id, rc.user().principal().getLong("account_id"))
                .flatMapCompletable(this::checkDeleteEntityIsUsed)
                .andThen(Single.just(id)))
            .flatMapCompletable(entityChecker::submitDelete);
    }

    private Ensemble createNewEnsemble(CreateEnsembleRequest request, long accountId) {
        Account createdBy = new Account();
        createdBy.setAccountId(accountId);
        Ensemble ensemble = new Ensemble();
        ensemble.setName(request.getName());
        ensemble.setCreatedBy(createdBy);
        ensemble.setRegions(request.getRegions());
        ensemble.setProviders(request.getProviders());
        ensemble.setResource_types(request.getResourceTypes());
        return ensemble;
    }

    private Completable createResourceEnsembles(Ensemble ensemble, List<ResourceId> resourceIds) {
        return Observable.fromIterable(resourceIds)
            .map(resourceId -> {
                Resource resource = new Resource();
                resource.setResourceId(resourceId.getResourceId());
                ResourceEnsemble resourceEnsemble = new ResourceEnsemble();
                resourceEnsemble.setEnsemble(ensemble);
                resourceEnsemble.setResource(resource);
                return JsonObject.mapFrom(resourceEnsemble);
            })
            .toList()
            .flatMapCompletable(resourceEnsembles -> {
                JsonArray resourceEnsembleArray = new JsonArray(resourceEnsembles);
                return resourceEnsembleChecker.submitCreateAll(resourceEnsembleArray);
            });
    }

    private Completable createEnsembleSLOs(Ensemble ensemble, List<ServiceLevelObjective> slos) {
        return Observable.fromIterable(slos)
            .map(slo -> {
                EnsembleSLO ensembleSLO = new EnsembleSLO();
                ensembleSLO.setName(slo.getName());
                ensembleSLO.setExpression(slo.getExpression());
                switch (slo.getValue().get(0).getSloValueType()) {
                    case NUMBER:
                        List<Double> numberValues = slo.getValue().stream()
                            .map(value -> (Double) value.getValueNumber()).collect(Collectors.toList());
                        ensembleSLO.setValueNumbers(numberValues);
                        break;
                    case STRING:
                        List<String> stringValues = slo.getValue().stream()
                            .map(SLOValue::getValueString).collect(Collectors.toList());
                        ensembleSLO.setValueStrings(stringValues);
                        break;
                    case BOOLEAN:
                        List<Boolean> boolValues = slo.getValue().stream()
                            .map(SLOValue::getValueBool).collect(Collectors.toList());
                        ensembleSLO.setValueBools(boolValues);
                        break;
                }
                ensembleSLO.setEnsemble(ensemble);
                return JsonObject.mapFrom(ensembleSLO);
            })
            .toList()
            .flatMapCompletable(ensembleSLOs -> {
                JsonArray ensembleSLOArray = new JsonArray(ensembleSLOs);
                return ensembleSLOChecker.submitCreateAll(ensembleSLOArray);
            });
    }
}
