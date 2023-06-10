package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDAO;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.entity.model.ServiceDeployment;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.deployment.ResourceDeploymentService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implements methods to perform CRUD operations on the resource_deployment entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ResourceDeploymentChecker extends EntityChecker {

    private final ResourceDeploymentService resourceDeploymentService;

    /**
     * Create an instance from the resourceDeploymentService.
     *
     * @param resourceDeploymentService the resource deployment service
     */
    public ResourceDeploymentChecker(ResourceDeploymentService resourceDeploymentService) {
        super(resourceDeploymentService);
        this.resourceDeploymentService = resourceDeploymentService;
    }

    /**
     * Find all resource deployments by deployment.
     *
     * @param id id of the deployment
     * @return a Single that emits all found resource deployments as JsonArray
     */
    public Single<JsonArray> checkFindAllByDeploymentId(long id) {
        Single<JsonArray> findAllByDeploymentId = resourceDeploymentService.findAllByDeploymentId(id);
        return ErrorHandler.handleFindAll(findAllByDeploymentId);
    }

    /**
     * Submit the update of the status of a resource deployment.
     *
     * @param deploymentId the id of the deployment
     * @param statusValue the new status
     * @return a Completable
     */
    public Completable submitUpdateStatus(long deploymentId, DeploymentStatusValue statusValue) {
        return resourceDeploymentService.updateSetStatusByDeploymentId(deploymentId, statusValue);
    }

    /**
     * Store the trigger urls of a deployment to the resource deployments.
     *
     * @param processOutput the output of the terraform process
     * @param request all data needed for the deployment process
     * @return a Completable
     */
    public Completable storeOutputToResourceDeployments(ProcessOutput processOutput, DeployResourcesDAO request) {
        DeploymentOutput deploymentOutput = DeploymentOutput.fromJson(new JsonObject(processOutput.getOutput()));
        List<Completable> completables = new ArrayList<>();
        completables.addAll(setTriggerUrlsByResourceTypeSet(deploymentOutput.getFunctionUrls().getValue().entrySet(),
            request));
        completables.addAll(setTriggerUrlForContainers(request));
        return Completable.merge(completables);
    }

    /**
     * Store all trigger urls of a deployment by resource type.
     *
     * @param resourceTypeSet all function resources of a certain resource type
     * @param request all data needed for the deployment process
     * @return a list of Completables
     */
    private List<Completable> setTriggerUrlsByResourceTypeSet(Set<Map.Entry<String, String>> resourceTypeSet,
                                                              DeployResourcesDAO request) {
        List<Completable> completables = new ArrayList<>();
        for (Map.Entry<String, String> entry : resourceTypeSet) {
            String[] entryInfo = entry.getKey().split("_");
            long resourceId = Long.parseLong(entryInfo[0].substring(1));
            String functionName = entryInfo[1], runtimeName = entryInfo[2];
            findFunctionResourceAndUpdateTriggerUrl(request, resourceId, functionName, runtimeName, entry.getValue(),
                completables);
        }
        return completables;
    }

    private List<Completable> setTriggerUrlForContainers(DeployResourcesDAO request) {
        List<Completable> completables = new ArrayList<>();
        for (ServiceDeployment serviceDeployment : request.getServiceDeployments()) {
            String triggerUrl = String.format("/deployments/%s/%s/startup",
                request.getDeployment().getDeploymentId(),
                serviceDeployment.getResourceDeploymentId()) ;
            completables.add(resourceDeploymentService.updateTriggerUrl(serviceDeployment.getResourceDeploymentId(),
                triggerUrl));
        }
        return completables;
    }

    /**
     * Find the persisted function resource and update its trigger url.
     *
     * @param request all data needed for the deployment process
     * @param resourceId the id of the resource
     * @param functionName the name of the function
     * @param runtimeName the name of the runtime
     * @param triggerUrl the trigger url
     * @param completables the list where to store the new completables
     */
    private void findFunctionResourceAndUpdateTriggerUrl(DeployResourcesDAO request, long resourceId,
        String functionName, String runtimeName, String triggerUrl, List<Completable> completables) {
        request.getFunctionDeployments().stream()
            .filter(functionDeployment -> matchesFunctionResource(resourceId, functionName, runtimeName,
                functionDeployment))
            .findFirst()
            .ifPresent(functionDeployment -> completables
                .add(resourceDeploymentService.updateTriggerUrl(functionDeployment.getResourceDeploymentId(),
                    triggerUrl))
            );
    }

    /**
     * Get the crucial resource deployment status based on all resource deployments of a single
     * deployment.
     *
     * @param resourceDeployments the resource deployments
     * @return the crucial deployment status
     */
    public DeploymentStatusValue checkCrucialResourceDeploymentStatus(JsonArray resourceDeployments) {
        if (matchAnyResourceDeploymentsStatus(resourceDeployments,
            DeploymentStatusValue.ERROR)) {
            return DeploymentStatusValue.ERROR;
        }
        if (matchAnyResourceDeploymentsStatus(resourceDeployments, DeploymentStatusValue.NEW)) {
            return DeploymentStatusValue.NEW;
        }

        if (matchAnyResourceDeploymentsStatus(resourceDeployments,
            DeploymentStatusValue.TERMINATING)) {
            return DeploymentStatusValue.TERMINATING;
        }

        if (matchAnyResourceDeploymentsStatus(resourceDeployments,
            DeploymentStatusValue.DEPLOYED)) {
            return DeploymentStatusValue.DEPLOYED;
        }
        return DeploymentStatusValue.TERMINATED;
    }

    /**
     * Check if at least one status of resourceDeployments matches the given status value.
     *
     * @param resourceDeployments the resource deployments
     * @param statusValue the status value
     * @return true if at least one match was found, else false
     */
    private boolean matchAnyResourceDeploymentsStatus(JsonArray resourceDeployments,
        DeploymentStatusValue statusValue) {
        return resourceDeployments.stream()
            .map(rr -> (JsonObject) rr)
            .anyMatch(rr -> rr.getJsonObject("status")
                .getString("status_value").equals(statusValue.name())
            );
    }

    /**
     * Check if the given parameters match the values of the given function resource
     *
     * @param deploymentId the id of the deployment
     * @param functionName the name of the function
     * @param runtimeName the name of the runtime
     * @param functionDeployment the function deployment
     * @return true if they match, else false
     */
    private static boolean matchesFunctionResource(long deploymentId, String functionName, String runtimeName,
        FunctionDeployment functionDeployment) {
        return functionDeployment.getResource().getResourceId() == deploymentId &&
            functionDeployment.getFunction().getName().equals(functionName) &&
            functionDeployment.getFunction().getRuntime().getName().replace(".", "").equals(runtimeName);
    }
}
