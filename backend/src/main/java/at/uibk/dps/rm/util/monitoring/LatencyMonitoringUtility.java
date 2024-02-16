package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.deployment.ProcessOutput;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.service.deployment.executor.ProcessExecutor;
import io.reactivex.rxjava3.core.Single;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

/**
 * Provides methods to measure latency between resources and the resource manager.
 *
 * @author matthi-g
 */
@UtilityClass
public class LatencyMonitoringUtility {

    /**
     * Measure the latency for numberRequests and between the resource manager and the pingUrl.
     *
     * @param numberRequests the amount of requests
     * @param pingUrl the url to use for the measurement
     * @return a Single that emits a {@link ProcessOutput}
     */
    public static Single<ProcessOutput> measureLatency(int numberRequests, String pingUrl) {
        Path scriptsPath = Path.of("monitoring").toAbsolutePath();
        String scriptArgs = " -c " + numberRequests + " -w " + pingUrl;
        List<String> commands;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            Path scriptPath = Path.of(scriptsPath.toString(), "latencytest.bat");
            commands = List.of("cmd.exe", "/c", scriptPath + scriptArgs);
        } else {
            Path scriptPath = Path.of(scriptsPath.toString(), "latencytest.sh");
            commands = List.of("bash", "-c", scriptPath + scriptArgs);
        }
        ProcessExecutor processExecutor = new ProcessExecutor(scriptsPath, commands);
        return processExecutor.executeCli();
    }

    /**
     * Get the ping url for an aws region.
     *
     * @param region the aws region
     * @return the ping url
     */
    public static String getPingUrlFromAwsRegion(Region region) {
        return "ec2." + region.getName() + ".amazonaws.com";
    }

    /**
     * Get the ping url from an url that ends with a path.
     *
     * @param urlWithPath the url with path
     * @return the ping url
     */
    public static String getPingUrl(String urlWithPath) throws URISyntaxException {
        URI uri = new URI(urlWithPath);
        return uri.getHost();
    }
}
