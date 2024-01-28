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

@UtilityClass
public class LatencyMonitoringUtility {

    public static Single<ProcessOutput> monitorLatency(int numberRequests, String pingUrl) {
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

    public static String getPingUrlFromAwsRegion(Region region) {
        return "ec2." + region.getName() + ".amazonaws.com";
    }

    public static String getPingUrlFromK8sBasePath(String k8sBasePath) throws URISyntaxException {
        URI uri = new URI(k8sBasePath);
        return uri.getHost();
    }
}
