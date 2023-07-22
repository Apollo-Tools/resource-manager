package at.uibk.dps.rm.handler.deployment;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.deployment.DeploymentStatusValue;
import at.uibk.dps.rm.entity.dto.deployment.DeployResourcesDTO;
import at.uibk.dps.rm.entity.model.Log;
import at.uibk.dps.rm.entity.model.Deployment;
import at.uibk.dps.rm.entity.model.DeploymentLog;
import at.uibk.dps.rm.handler.deploymentexecution.DeploymentExecutionHandler;
import at.uibk.dps.rm.handler.log.LogChecker;
import at.uibk.dps.rm.handler.log.DeploymentLogChecker;
import at.uibk.dps.rm.handler.util.FileSystemChecker;
import at.uibk.dps.rm.util.configuration.ConfigUtility;
import io.reactivex.rxjava3.core.Completable;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;

/**
 * Handles errors that may occur during the deployment and termination of resources.
 *
 * @author matthi-g
 */
public class DeploymentErrorHandler {

    private final ResourceDeploymentChecker resourceDeploymentChecker;

    private final LogChecker logChecker;

    private final DeploymentLogChecker deploymentLogChecker;

    private final FileSystemChecker fileSystemChecker;

    private final DeploymentExecutionHandler deploymentHandler;

    /**
     * Create an instance from the resourceDeploymentChecker, logChecker, deploymentLogChecker,
     * fileSystemChecker and deploymentHandler.
     *
     * @param resourceDeploymentChecker the resource deployment checker
     * @param logChecker the log checker
     * @param deploymentLogChecker the deployment log checker
     * @param fileSystemChecker the file system checker
     * @param deploymentHandler the deployment handler
     */
    public DeploymentErrorHandler(ResourceDeploymentChecker resourceDeploymentChecker, LogChecker logChecker,
                                   DeploymentLogChecker deploymentLogChecker, FileSystemChecker fileSystemChecker,
                                   DeploymentExecutionHandler deploymentHandler) {
        this.resourceDeploymentChecker = resourceDeploymentChecker;
        this.logChecker = logChecker;
        this.deploymentLogChecker = deploymentLogChecker;
        this.fileSystemChecker = fileSystemChecker;
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
        Vertx vertx = Vertx.currentContext().owner();
        return handleError(deployResources.getDeployment(), throwable)
            .andThen(new ConfigUtility(vertx).getConfig()
                .flatMap(config -> {
                    String path = new DeploymentPath(deployResources.getDeployment().getDeploymentId(), config)
                        .getRootFolder().toString();
                    return fileSystemChecker.checkTFLockFileExists(path);
                }))
            .flatMapCompletable(tfLockFileExists -> {
                if (tfLockFileExists) {
                    return deploymentHandler.terminateResources(deployResources)
                        .onErrorComplete();
                } else {
                    return Completable.complete();
                }
            });
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
        return resourceDeploymentChecker
            .submitUpdateStatus(deployment.getDeploymentId(), DeploymentStatusValue.ERROR)
            .toSingle(() -> {
                Log log = new Log();
                log.setLogValue(throwable.getMessage());
                return logChecker.submitCreate(JsonObject.mapFrom(log));
            })
            .flatMap(res -> res)
            .flatMap(logResult -> {
                Log logStored = logResult.mapTo(Log.class);
                DeploymentLog deploymentLog = new DeploymentLog();
                deploymentLog.setDeployment(deployment);
                deploymentLog.setLog(logStored);
                return deploymentLogChecker.submitCreate(JsonObject.mapFrom(deploymentLog));
            })
            .ignoreElement();
    }
}
