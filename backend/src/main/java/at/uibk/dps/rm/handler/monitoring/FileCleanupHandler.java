package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.deployment.DeploymentPath;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.core.file.FileSystem;
import lombok.RequiredArgsConstructor;

/**
 * This monitoring handler checks the file system for deployments, that ended up in a failing state
 * and deletes them.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class FileCleanupHandler implements MonitoringHandler {

    private static final Logger logger = LoggerFactory.getLogger(FileCleanupHandler.class);

    private final Vertx vertx;

    private final ConfigDTO configDTO;

    private final ServiceProxyProvider serviceProxyProvider;

    private long currentTimer = -1L;

    @Override
    public void startMonitoringLoop() {
        long period = (long) (configDTO.getFileCleanupPeriod() * 1000);
        Handler<Long> cleanupHandler = id -> {
            logger.info("Started: cleanup of build directory");
            FileSystem fileSystem = vertx.fileSystem();
            fileSystem.readDir(configDTO.getBuildDirectory(), "^deployment_.*")
                .flatMapObservable(Observable::fromIterable)
                .map(entry -> {
                    String deploymentId = entry.split("_")[1];
                    try {
                        return Long.parseLong(deploymentId);
                    } catch (NumberFormatException e) {
                        return -1L;
                    }
                })
                .toList()
                .flatMap(deploymentIds -> {
                    logger.info("Active deployments: " + deploymentIds);
                    return serviceProxyProvider.getDeploymentService().findAllWithErrorStateByIds(deploymentIds);
                })
                .flatMapObservable(Observable::fromIterable)
                .flatMapCompletable(deployment -> {
                    long deploymentId = ((JsonObject) deployment).getLong("deployment_id");
                    logger.info("Delete deployment directory: " + deploymentId);
                    DeploymentPath deploymentPath = new DeploymentPath(deploymentId, configDTO);
                    return fileSystem.deleteRecursive(deploymentPath.getRootFolder().toString(), true);
                })
                .subscribe(() -> logger.info("Finished: file cleanup"),
                    throwable -> logger.error(throwable.getMessage()));
        };
        vertx.setPeriodic(0L, period, cleanupHandler);
    }

    @Override
    public void pauseMonitoringLoop() {
        vertx.cancelTimer(currentTimer);
        currentTimer = -1L;
    }
}
