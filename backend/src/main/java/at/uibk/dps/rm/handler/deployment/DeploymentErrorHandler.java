package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionChecker;
import at.uibk.dps.rm.router.deployment.DeploymentRoute;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.Vertx;

/**
 * Handles errors that may occur during the deployment and termination of resources.
 *
 * @author matthi-g
 */
public class DeploymentErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentRoute.class);

    private final DeploymentChecker deploymentChecker;

    private final DeploymentExecutionChecker deploymentExecutionChecker;

    /**
     * Create an instance from the deploymentChecker and deploymentExecutionChecker.
     *
     * @param deploymentChecker the deployment checker
     * @param deploymentExecutionChecker the deployment execution checker
     */
    public DeploymentErrorHandler(DeploymentChecker deploymentChecker,
            DeploymentExecutionChecker deploymentExecutionChecker) {
        this.deploymentChecker = deploymentChecker;
        this.deploymentExecutionChecker = deploymentExecutionChecker;
    }

    public void handleDeployResources(Completable deployment, DeployResourcesDTO deployResources) {
        deployment.doOnError(throwable -> logger.error(throwable.getMessage()))
            .onErrorResumeNext(throwable ->
                deploymentChecker.handleDeploymentError(deployResources.getDeployment().getDeploymentId(),
                        throwable.getMessage())
                    .andThen(this.terminateFailedDeployment(deployResources)))
            .subscribe();
    }

    public void handleTerminateResources(Completable termination, long deploymentId) {
        termination.doOnError(throwable -> logger.error(throwable.getMessage()))
            .onErrorResumeNext(throwable ->
                deploymentChecker.handleDeploymentError(deploymentId, throwable.getMessage()))
            .subscribe();
    }

    /**
     * Terminate all resources from the deployment.
     *
     * @param deployResources the data of the deployment
     * @return a Completable
     */
    private Completable terminateFailedDeployment(DeployResourcesDTO deployResources) {
        TerminateResourcesDTO terminateResources = new TerminateResourcesDTO(deployResources);
        Vertx vertx = Vertx.currentContext().owner();
        return new ConfigUtility(vertx).getConfig()
            .flatMap(config -> {
                String path = new DeploymentPath(deployResources.getDeployment().getDeploymentId(), config)
                    .getRootFolder().toString();
                return deploymentExecutionChecker.tfLockFileExists(path);
            })
            .flatMapCompletable(locFileExists -> {
                if (locFileExists) {
                    return deploymentExecutionChecker.terminateResources(terminateResources).onErrorComplete();
                }
                return Completable.complete();
            })
            .andThen(Completable.defer(() ->
                deploymentExecutionChecker.deleteTFDirs(deployResources.getDeployment().getDeploymentId())));
    }
}
