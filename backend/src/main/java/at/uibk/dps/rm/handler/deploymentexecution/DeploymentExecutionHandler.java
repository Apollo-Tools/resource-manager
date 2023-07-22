package at.uibk.dps.rm.handler.deploymentexecution;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.handler.deployment.ResourceDeploymentChecker;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.rxjava3.core.Vertx;

/**
 * Processes requests that concern deployment.
 *
 * @author matthi-g
 */
public class DeploymentExecutionHandler {

    private final DeploymentExecutionChecker deploymentChecker;

    private final ResourceDeploymentChecker resourceDeploymentChecker;

    /**
     * Create an instance from the deploymentChecker and resourceDeploymentChecker.
     *
     * @param deploymentChecker the deployment checker
     * @param resourceDeploymentChecker the resource deployment checker
     */
    public DeploymentExecutionHandler(DeploymentExecutionChecker deploymentChecker,
            ResourceDeploymentChecker resourceDeploymentChecker) {
        this.deploymentChecker = deploymentChecker;
        this.resourceDeploymentChecker = resourceDeploymentChecker;
    }

    /**
     * Deploy all resources from the deployResources object. The
     * docker credentials must contain valid data for all deployments that involve OpenFaaS. The
     * list of VPCs must be non-empty for EC2 deployments.
     *
     * @param deployResources the data of the deployment
     * @return a Completable
     */
    public Completable deployResources(DeployResourcesDTO deployResources) {
        return deploymentChecker.applyResourceDeployment(deployResources)
            .flatMapCompletable(tfOutput -> Completable.defer(() -> resourceDeploymentChecker
                .storeOutputToResourceDeployments(tfOutput, deployResources)))
            .andThen(Completable.defer(() ->
                resourceDeploymentChecker.submitUpdateStatus(deployResources.getDeployment().getDeploymentId(),
                    DeploymentStatusValue.DEPLOYED)));
    }

    /**
     * Terminate all resources from the deployment that should be terminated.
     *
     * @param terminateResources the data of the deployment
     * @return a Completable
     */
    public Completable terminateResources(TerminateResourcesDTO terminateResources) {
        long deploymentId = terminateResources.getDeployment().getDeploymentId();
        return deploymentChecker.terminateResources(terminateResources)
            .andThen(Completable.defer(() -> deploymentChecker.deleteTFDirs(deploymentId)))
            .andThen(Completable.defer(() -> resourceDeploymentChecker.submitUpdateStatus(deploymentId,
                DeploymentStatusValue.TERMINATED)));
    }

    /**
     * Terminate all resources from the deployment.
     *
     * @param deployResources the data of the deployment
     * @return a Completable
     */
    public Completable terminateResources(DeployResourcesDTO deployResources) {
        TerminateResourcesDTO terminateResources = new TerminateResourcesDTO(deployResources);
        Vertx vertx = Vertx.currentContext().owner();
        return new ConfigUtility(vertx).getConfig()
            .flatMap(config -> {
                String path = new DeploymentPath(deployResources.getDeployment().getDeploymentId(), config)
                    .getRootFolder().toString();
                return deploymentChecker.tfLockFileExists(path);
            })
            .flatMapCompletable(locFileExists -> {
                if (locFileExists) {
                    return deploymentChecker.terminateResources(terminateResources).onErrorComplete();
                }
                return Completable.complete();
            })
            .andThen(Completable.defer(() ->
                deploymentChecker.deleteTFDirs(deployResources.getDeployment().getDeploymentId())));
    }
}
