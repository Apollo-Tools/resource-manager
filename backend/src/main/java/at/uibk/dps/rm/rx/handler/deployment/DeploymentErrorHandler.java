package at.uibk.dps.rm.rx.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.dto.deployment.TerminateResourcesDTO;
import at.uibk.dps.rm.rx.handler.deploymentexecution.DeploymentExecutionChecker;
import at.uibk.dps.rm.router.deployment.DeploymentRoute;
import at.uibk.dps.rm.rx.service.rxjava3.database.deployment.DeploymentService;
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

    private final DeploymentService deploymentService;

    private final DeploymentExecutionChecker deploymentExecutionChecker;

    /**
     * Create an instance from the deploymentService and deploymentExecutionChecker.
     *
     * @param deploymentService the deployment service
     * @param deploymentExecutionChecker the deployment execution checker
     */
    public DeploymentErrorHandler(DeploymentService deploymentService,
            DeploymentExecutionChecker deploymentExecutionChecker) {
        this.deploymentService = deploymentService;
        this.deploymentExecutionChecker = deploymentExecutionChecker;
    }

    /**
     * Handle errors for a deployment process.
     *
     * @param deployment the deployment process
     * @param deployResources the deployment request
     */
    public void handleDeployResources(Completable deployment, DeployResourcesDTO deployResources) {
        deployment.doOnError(throwable -> logger.error(throwable.getMessage()))
            .onErrorResumeNext(throwable ->
                deploymentService.handleDeploymentError(deployResources.getDeployment().getDeploymentId(),
                        throwable.getMessage())
                    .andThen(this.terminateFailedDeployment(deployResources)))
            .subscribe();
    }

    /**
     * Handle errors for the termination process.
     *
     * @param termination the termination process
     * @param deploymentId the deployment id
     */
    public void handleTerminateResources(Completable termination, long deploymentId) {
        termination.doOnError(throwable -> logger.error(throwable.getMessage()))
            .onErrorResumeNext(throwable ->
                deploymentService.handleDeploymentError(deploymentId, throwable.getMessage()))
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
        return new ConfigUtility(vertx).getConfigDTO()
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
