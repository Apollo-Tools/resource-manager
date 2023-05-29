package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.dto.slo.*;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.util.misc.HttpHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Processes the http requests that concern the ensemble entity.
 *
 * @author matthi-g
 */
public class EnsembleHandler extends ValidationHandler {

    private final EnsembleChecker ensembleChecker;

    private final EnsembleSLOChecker ensembleSLOChecker;

    private final ResourceEnsembleChecker resourceEnsembleChecker;

    private final ResourceChecker resourceChecker;

    private final ObjectMapper mapper;

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
        this.mapper = DatabindCodec.mapper();
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
    public Single<JsonObject> getOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> ensembleChecker.checkFindOne(id, rc.user().principal().getLong("account_id")))
            .flatMap(this::populateEnsembleDetails);
    }

    public Single<JsonObject> getOne(long id) {
        return ensembleChecker.checkFindOne(id)
            .flatMap(this::populateEnsembleDetails);
    }

    private Single<JsonObject> populateEnsembleDetails(JsonObject ensembleJson) {
        GetOneEnsemble response = new GetOneEnsemble();
        Ensemble ensemble = ensembleJson.mapTo(Ensemble.class);
        response.setEnsembleId(ensemble.getEnsembleId());
        response.setName(ensemble.getName());
        response.setCreatedAt(ensemble.getCreatedAt());
        response.setUpdatedAt(ensemble.getUpdatedAt());
        return resourceChecker.checkFindAllByEnsemble(ensemble.getEnsembleId())
            .flatMap(resources -> {
                mapResourcesToResponse(resources, response);
                return ensembleSLOChecker.checkFindAllByEnsemble(ensemble.getEnsembleId());
            })
            .map(slos -> {
                mapSLOsToResponse(slos, ensemble, response);
                return JsonObject.mapFrom(response);
            });
    }

    @Override
    public Single<JsonArray> getAll(RoutingContext rc) {
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

    /**
     * Create a new ensemble instance from the request and accountId
     *
     * @param request the createEnsembleRequest
     * @param accountId the id of the account
     * @return the new ensemble
     */
    private Ensemble createNewEnsemble(CreateEnsembleRequest request, long accountId) {
        Account createdBy = new Account();
        createdBy.setAccountId(accountId);
        Ensemble ensemble = new Ensemble();
        ensemble.setIsValid(true);
        ensemble.setName(request.getName());
        ensemble.setCreatedBy(createdBy);
        ensemble.setRegions(request.getRegions());
        ensemble.setProviders(request.getProviders());
        ensemble.setResource_types(request.getResourceTypes());
        return ensemble;
    }

    /**
     * Create and persist resource ensembles with the ensemble and resourceIds.
     *
     * @param ensemble the ensemble
     * @param resourceIds the list of resource ids
     * @return a Completable
     */
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

    /**
     * Create and persist ensemble slos with the ensemble and slos.
     *
     * @param ensemble the ensemble
     * @param slos the list of service level objectives
     * @return a Completable
     */
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

    /**
     * Map the ensembleSLOs model entity to the ServiceLevelObjective DTO.
     *
     * @param ensembleSLOs the list of ensembleSLOs
     * @return a List of mapped ServiceLevelObjectives
     */
    private List<ServiceLevelObjective> mapEnsembleSLOtoDTO(List<EnsembleSLO> ensembleSLOs) {
        return ensembleSLOs.stream()
            .map(ensembleSLO -> {
                List<SLOValue> sloValues;
                if (ensembleSLO.getValueNumbers() != null) {
                    sloValues = ensembleSLO.getValueNumbers().stream().map(value -> {
                        SLOValue sloValue = new SLOValue();
                        sloValue.setValueNumber(value);
                        sloValue.setSloValueType(SLOValueType.NUMBER);
                        return sloValue;
                    }).collect(Collectors.toList());
                } else if (ensembleSLO.getValueStrings() != null) {
                    sloValues = ensembleSLO.getValueStrings().stream().map(value -> {
                        SLOValue sloValue = new SLOValue();
                        sloValue.setValueString(value);
                        sloValue.setSloValueType(SLOValueType.STRING);
                        return sloValue;
                    }).collect(Collectors.toList());
                } else {
                    sloValues = ensembleSLO.getValueBools().stream().map(value -> {
                        SLOValue sloValue = new SLOValue();
                        sloValue.setValueBool(value);
                        sloValue.setSloValueType(SLOValueType.BOOLEAN);
                        return sloValue;
                    }).collect(Collectors.toList());
                }
                return new ServiceLevelObjective(ensembleSLO.getName(),
                    ensembleSLO.getExpression(), sloValues);
            }).collect(Collectors.toList());
    }

    /**
     * Map an JsonArray of resources to the response.
     *
     * @param resources the resources
     * @param response the response
     * @throws JsonProcessingException when a json processing error occurs
     */
    private void mapResourcesToResponse(JsonArray resources, GetOneEnsemble response)
        throws JsonProcessingException {
        List<Resource> resourceList = mapper.readValue(resources.toString(), new TypeReference<>() {});
        response.setResources(resourceList);
    }

    /**
     * Map an JsonArray of service level objectives and the regions, resource providers and
     * resource types of the ensemble to the response.
     *
     * @param slos the service level objectives
     * @param ensemble the ensemble
     * @param response the response
     * @throws JsonProcessingException when a json processing error occurs
     */
    private void mapSLOsToResponse(JsonArray slos, Ensemble ensemble, GetOneEnsemble response)
        throws JsonProcessingException {
        List<EnsembleSLO> ensembleSLOs = mapper.readValue(slos.toString(), new TypeReference<>() {});
        List<ServiceLevelObjective> serviceLevelObjectives = mapEnsembleSLOtoDTO(ensembleSLOs);
        response.setServiceLevelObjectives(serviceLevelObjectives);
        response.setRegions(ensemble.getRegions());
        response.setProviders(ensemble.getProviders());
        response.setResourceTypes(ensemble.getResource_types());
    }
}
