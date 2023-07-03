package at.uibk.dps.rm.handler.resource;

import at.uibk.dps.rm.entity.dto.CreateEnsembleRequest;
import at.uibk.dps.rm.entity.dto.ensemble.GetOneEnsemble;
import at.uibk.dps.rm.entity.dto.ensemble.ResourceEnsembleStatus;
import at.uibk.dps.rm.entity.dto.resource.ResourceId;
import at.uibk.dps.rm.entity.model.Resource;
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
import java.util.Objects;

/**
 * Processes the http requests that concern filtering resources by Service Level Objectives.
 *
 * @author matthi-g
 */
public class ResourceSLOHandler {

    private final ResourceChecker resourceChecker;

    /**
     * Create an instance from the resourceChecker, metricChecker and metricValueChecker.
     *
     * @param resourceChecker the resource checker
     */
    public ResourceSLOHandler(ResourceChecker resourceChecker) {
        this.resourceChecker = resourceChecker;
    }

    /**
     * Validate the resources for a create ensemble request.
     *
     * @param rc the routing context
     */
    public void validateNewResourceEnsembleSLOs(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        resourceChecker.checkFindAllBySLOs(requestBody)
            .flatMapCompletable(resources -> {
                CreateEnsembleRequest requestDTO = requestBody.mapTo(CreateEnsembleRequest.class);
                List<ResourceId> resourceIds = requestDTO.getResources();
                return checkResourcesFulfillSLOs(resourceIds, resources);
            })
            .subscribe(rc::next, throwable -> rc.fail(400, throwable));
    }

    /**
     * Validate the resources from an existing ensemble.
     *
     * @param requestBody the request body
     * @return a Single that emits the List of pairs of resource_ids and their validation status
     */
    public Single<List<ResourceEnsembleStatus>> validateExistingEnsemble(JsonObject requestBody) {
        GetOneEnsemble ensemble = requestBody.mapTo(GetOneEnsemble.class);
        return resourceChecker.checkFindAllBySLOs(requestBody)
            .flatMap(resources -> Observable.fromIterable(ensemble.getResources())
                .map(resource -> {
                    ResourceId resourceId = new ResourceId();
                    resourceId.setResourceId(resource.getResourceId());
                    return resourceId;
                })
                .toList()
                .flatMap(resourceIds -> getResourceEnsembleStatus(resourceIds, resources)));
    }

    /**
     * Get the slo status of all resources
     *
     * @param validResourceIds the ids of all valid resources
     * @param resources the resources to validate
     * @return the List of pairs of resource_ids and their validation status being true for valid
     * and false for invalid
     * @throws JsonProcessingException  when an error occurs during json processing
     */
    public Single<List<ResourceEnsembleStatus>> getResourceEnsembleStatus(List<ResourceId> validResourceIds,
            JsonArray resources) throws JsonProcessingException {
        ObjectMapper mapper = DatabindCodec.mapper();
        List<Resource> resourceList = mapper.readValue(resources.toString(), new TypeReference<>() {});
        return Observable.fromIterable(validResourceIds)
            .map(ResourceId::getResourceId)
            .map(resourceId -> {
                boolean isValid = resourceList.stream()
                    .anyMatch(resource -> Objects.equals(resource.getResourceId(), resourceId));
                return new ResourceEnsembleStatus(resourceId, isValid);
            })
            .toList();
    }

    /**
     * Check if resources fulfill service level objectives.
     *
     * @param validResourceIds the ids of all valid resources
     * @param resources the resources to validate
     * @return a Completable if all resources fulfill the slos else a throwable is thrown
     * @throws JsonProcessingException when an error occurs during json processing
     */
    public Completable checkResourcesFulfillSLOs(List<ResourceId> validResourceIds, JsonArray resources)
        throws JsonProcessingException {
        ObjectMapper mapper = DatabindCodec.mapper();
        List<Resource> resourceList = mapper.readValue(resources.toString(), new TypeReference<>() {});
        return Observable.fromIterable(validResourceIds)
            .map(ResourceId::getResourceId)
            .all(resourceId -> resourceList.stream()
                .anyMatch(resource -> Objects.equals(resource.getResourceId(), resourceId)))
            .map(result -> {
                if (!result) {
                    throw new Throwable("slo mismatch");
                }
                return true;
            })
            .ignoreElement();
    }
}
