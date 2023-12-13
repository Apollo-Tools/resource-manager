package at.uibk.dps.rm.handler.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.resource.ResourceProviderEnum;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.RegionConnectivity;
import at.uibk.dps.rm.service.ServiceProxyProvider;
import at.uibk.dps.rm.service.deployment.executor.ProcessExecutor;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.Handler;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.core.Vertx;
import lombok.RequiredArgsConstructor;

import java.nio.file.Path;
import java.util.List;

/**
 * This monitoring handler monitors registered regions. This includes checking if a regions is
 * reachable and measuring the latency to that region.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class RegionMonitoringHandler implements MonitoringHandler {
    private static final Logger logger = LoggerFactory.getLogger(RegionMonitoringHandler.class);

    private final Vertx vertx;

    private final ConfigDTO configDTO;

    private long currentTimer = -1L;

    private boolean pauseLoop = false;

    private static Handler<Long> monitoringHandler;

    @Override
    public void startMonitoringLoop() {
        pauseLoop = false;
        long period = (long) (configDTO.getRegionMonitoringPeriod() * 60 * 1000);
        ServiceProxyProvider serviceProxyProvider = new ServiceProxyProvider(vertx);
        monitoringHandler = id -> serviceProxyProvider.getRegionService().findAll()
            .flatMapObservable(Observable::fromIterable)
            .map(region -> ((JsonObject) region).mapTo(Region.class))
            .filter(region -> {
                ResourceProviderEnum resourceProvider = ResourceProviderEnum
                    .fromString(region.getResourceProvider().getProvider());
                return resourceProvider.equals(ResourceProviderEnum.AWS);
            })
            .flatMapSingle(region -> {
                int numberRequests = 5;
                logger.info("Monitor latency: " + region.getName());
                String pingUrl = "ec2." + region.getName() + ".amazonaws.com";
                Path resourcePath = Path.of("src", "main", "resources", "monitoring").toAbsolutePath();
                String scriptArgs = " -c " + numberRequests + " -w " + pingUrl;
                List<String> commands;
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    Path scriptPath = Path.of(resourcePath.toString(), "latencytest.bat");
                    commands = List.of("cmd.exe", "/c", scriptPath + scriptArgs);
                } else {
                    Path scriptPath = Path.of(resourcePath.toString(), "latencytest.sh");
                    commands = List.of("bash", "-c", scriptPath + scriptArgs);
                }
                ProcessExecutor processExecutor = new ProcessExecutor(resourcePath, commands);
                return processExecutor.executeCli()
                    .map(processOutput -> {
                        RegionConnectivity connectivity = new RegionConnectivity();
                        connectivity.setRegion(region);
                        if (processOutput.getProcess().exitValue() == 0) {
                            connectivity.setIsOnline(true);
                            connectivity.setLatencyMs(Integer.parseInt(processOutput.getOutput()));
                        } else {
                            connectivity.setIsOnline(false);
                        }
                        return connectivity;
                    });
            })
            .toList()
            .flatMapCompletable(connectivities -> {
                JsonArray serializedConnectivities = new JsonArray(Json.encode(connectivities));
                return serviceProxyProvider.getRegionService().saveAllRegionConnectivities(serializedConnectivities);
            })
            .subscribe(() -> {
                logger.info("Finished: monitor regions");
                currentTimer = pauseLoop ? currentTimer : vertx.setTimer(period, monitoringHandler);
            }, throwable -> {
                logger.error(throwable.getMessage());
                currentTimer = pauseLoop ? currentTimer : vertx.setTimer(period, monitoringHandler);
            });
        currentTimer = vertx.setTimer(period, monitoringHandler);
    }

    @Override
    public void pauseMonitoringLoop() {
        pauseLoop = true;
        if (!vertx.cancelTimer(currentTimer)) {
            vertx.cancelTimer(currentTimer);
        }
        currentTimer = -1L;
    }
}
