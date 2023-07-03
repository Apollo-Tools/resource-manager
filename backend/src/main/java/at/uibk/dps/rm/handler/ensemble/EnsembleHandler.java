package at.uibk.dps.rm.handler.ensemble;

import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.slo.*;
import at.uibk.dps.rm.entity.model.*;
import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.util.misc.HttpHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ResourceChecker resourceChecker;

    private final ObjectMapper mapper;

    /**
     * Create an instance from the ensembleChecker, ensembleSLOChecker and resourceChecker.
     *
     * @param ensembleChecker the ensemble checker
     * @param ensembleSLOChecker the ensemble slo checker
     * @param resourceChecker the resource checker
     */
    public EnsembleHandler(EnsembleChecker ensembleChecker, EnsembleSLOChecker ensembleSLOChecker,
            ResourceChecker resourceChecker) {
        super(ensembleChecker);
        this.ensembleChecker = ensembleChecker;
        this.ensembleSLOChecker = ensembleSLOChecker;
        this.resourceChecker = resourceChecker;
        this.mapper = DatabindCodec.mapper();
    }

    // TODO: create DTO and add resources and slos to result
    @Override
    public Single<JsonObject> getOne(RoutingContext rc) {
        long accountId = rc.user().principal().getLong("account_id");
        return HttpHelper.getLongPathParam(rc, "id")
            .flatMap(id -> ensembleChecker.checkFindOne(id, accountId))
            .flatMap(this::populateEnsembleDetails);
    }

    /**
     * Get one ensemble by it's id.
     *
     * @param id the id of the ensemble
     * @return a JsonObject that emits the ensemble if it exists, else a NotFoundException is emitted
     */
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
        response.setPlatforms(ensemble.getPlatforms());
        response.setEnvironments(ensemble.getEnvironments());
    }
}
