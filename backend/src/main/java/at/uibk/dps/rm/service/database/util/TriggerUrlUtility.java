package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.deployment.output.DeploymentOutput;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.model.FunctionDeployment;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.DeploymentRepositoryProvider;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;

/**
 * A utility class that provides various methods to update the trigger urls of resource deployments.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class TriggerUrlUtility {

    private final DeploymentRepositoryProvider repositoryProvider;

    /**
     * Store all function trigger urls of a deployment.
     *
     * @param deploymentOutput the output of the deployment that contains the function urls
     * @param request all data needed for the deployment process
     * @return a Completable
     */
    public Completable setTriggerUrlsForFunctions(SessionManager sm,
            DeploymentOutput deploymentOutput, DeployResourcesDTO request) {
        return Observable.fromIterable(deploymentOutput.getFunctionUrls().getValue().entrySet())
            .flatMapCompletable(entry -> {
                String[] entryInfo = entry.getKey().split("_");
                long resourceId = Long.parseLong(entryInfo[0].substring(1));
                String functionName = entryInfo[1], runtimeName = entryInfo[2];
                return findFunctionDeploymentAndUpdateTriggerUrl(sm, request, resourceId, functionName,
                    runtimeName, entry.getValue());
            });
    }

    /**
     * Find the persisted function deployment and update its trigger url.
     *
     * @param request all data needed for the deployment process
     * @param resourceId the id of the resource
     * @param functionName the name of the function
     * @param runtimeName the name of the runtime
     * @param directTriggerUrl the trigger url
     * @return a Completable
     */
    private Completable findFunctionDeploymentAndUpdateTriggerUrl(SessionManager sm, DeployResourcesDTO request,
            long resourceId, String functionName, String runtimeName, String directTriggerUrl) {
        return Observable.fromIterable(request.getFunctionDeployments())
            .filter(functionDeployment -> matchesFunctionDeployment(resourceId, functionName, runtimeName,
                functionDeployment))
            .firstElement()
            .switchIfEmpty(Single.error(new NotFoundException("trigger url could not be set up for function " +
                "deployment")))
            .flatMapCompletable(functionDeployment -> {
                String rmTriggerUrl = String.format("/deployments/%s/resource-deployment/%s/invoke",
                    request.getDeployment().getDeploymentId(), functionDeployment.getResourceDeploymentId());
                return repositoryProvider.getFunctionDeploymentRepository().updateTriggerUrls(sm,
                    functionDeployment.getResourceDeploymentId(), rmTriggerUrl, directTriggerUrl);
            });
    }

    /**
     * Check if the given parameters match the values of the given function resource
     *
     * @param resourceId the id of the resource
     * @param functionName the name of the function
     * @param runtimeName the name of the runtime
     * @param functionDeployment the function deployment
     * @return true if they match, else false
     */
    private static boolean matchesFunctionDeployment(long resourceId, String functionName, String runtimeName,
            FunctionDeployment functionDeployment) {
        return functionDeployment.getResource().getResourceId() == resourceId &&
            functionDeployment.getFunction().getName().equals(functionName) &&
            functionDeployment.getFunction().getRuntime().getName().replace(".", "").equals(runtimeName);
    }

    /**
     * Store all trigger urls of container deployments.
     *
     * @param sm the database session manager
     * @param request all data needed for the deployment process
     * @return a Completable
     */
    public Completable setTriggerUrlForContainers(SessionManager sm, DeployResourcesDTO request) {
        return Observable.fromIterable(request.getServiceDeployments())
            .flatMapCompletable(serviceDeployment -> {
                String rmTriggerUrl = String.format("/deployments/%s/resource-deployments/%s/startup",
                    request.getDeployment().getDeploymentId(), serviceDeployment.getResourceDeploymentId()) ;
                return repositoryProvider.getResourceDeploymentRepository()
                    .updateRmTriggerUrl(sm, serviceDeployment.getResourceDeploymentId(), rmTriggerUrl);
            });
    }
}
