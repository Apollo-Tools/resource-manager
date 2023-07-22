package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionHandler;
import io.reactivex.rxjava3.core.Completable;

/**
 * Handles errors that may occur during the deployment and termination of resources.
 *
 * @author matthi-g
 */
public class DeploymentErrorHandler {

    private final DeploymentChecker deploymentChecker;

    private final DeploymentExecutionHandler deploymentHandler;

    /**
     * Create an instance from the deploymentChecker and deploymentHandler.
     *
     * @param deploymentChecker the deployment checker
     * @param deploymentHandler the deployment handler
     */
    public DeploymentErrorHandler(DeploymentChecker deploymentChecker, DeploymentExecutionHandler deploymentHandler) {
        this.deploymentChecker = deploymentChecker;
        this.deploymentHandler = deploymentHandler;
    }

    /**
     * Handle an error that occurred during deployment.
     *
     * @param deployResources the data of the deployment
     * @param throwable the thrown error
     * @return a Completable
     */
    public Completable onDeploymentError(DeployResourcesDTO deployResources, Throwable throwable) {
        return handleError(deployResources.getDeployment(), throwable)
            .andThen(deploymentHandler.terminateResources(deployResources));
    }

    /**
     * Handle an error that occurred during termination.
     *
     * @param deployment the deployment
     * @param throwable the thrown error
     * @return a Completable
     */
    public Completable onTerminationError(Deployment deployment, Throwable throwable) {
        return handleError(deployment, throwable);
    }

    /**
     * Handle an error of a deployment.
     *
     * @param deployment the deployment
     * @param throwable the thrown error
     * @return a Completable
     */
    private Completable handleError(Deployment deployment, Throwable throwable) {
        return deploymentChecker.handleDeploymentError(deployment.getDeploymentId(), throwable.getMessage());
    }
}
