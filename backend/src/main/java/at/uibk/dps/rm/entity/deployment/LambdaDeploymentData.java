package at.uibk.dps.rm.entity.deployment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.nio.file.Path;

/**
 * This class represents all the data for a deployment to AWS Lambda.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
@Getter
public class LambdaDeploymentData {
    private final long deploymentId;
    private final Path layerPath;
    private String functionNames = "";
    private String paths = "";
    private String handlers = "";
    private String timeouts = "";
    private String memorySizes = "";
    private String layers = "";
    private String runtimes = "";
    private String deploymentRoles = "";

    @Getter
    private long functionCount = 0;

    /**
     * Append new values to the currently stored values.
     *
     * @param functionName the name of the function
     * @param path the path to the packaged function
     * @param handler the entry point of the function
     * @param timeout the timeout of the function
     * @param memorySize the memory size of the function
     * @param layer the name of the function layer
     * @param runtime the name of the function runtime
     * @param deploymentRole the AWS role used for deployment
     */
    public void appendValues(String functionName, String path, String handler, BigDecimal timeout,
            BigDecimal memorySize, String layer, String runtime, String deploymentRole) {
        this.functionNames += addQuotes(functionName);
        this.paths += addQuotes(path);
        this.handlers += addQuotes(handler);
        this.timeouts += timeout + ",";
        this.memorySizes += memorySize + ",";
        this.layers += layer + ",";
        this.runtimes += addQuotes(runtime);
        this.deploymentRoles += addQuotes(deploymentRole);
        functionCount++;
    }

    /**
     * Add quotes to a string value.
     *
     * @param value the string value
     * @return the quoted string
     */
    private String addQuotes(String value) {
        return "\"" + value + "\",";
    }

    /**
     * Get the all module definitions composed by the stored values.
     *
     * @return the module definitions
     */
    public String getModuleString() {
        if (functionCount == 0) {
            return "";
        }

        String layerRootPath = this.layerPath.toAbsolutePath().toString().replace("\\", "/");
        return String.format(
            "module \"lambda\" {\n" +
                "  source = \"../../../terraform/aws/faas\"\n" +
                "  deployment_id = %s\n" +
                "  names = [%s]\n" +
                "  paths = [%s]\n" +
                "  handlers = [%s]\n" +
                "  timeouts = [%s]\n" +
                "  memory_sizes = [%s]\n" +
                "  layers = {layers=[%s], path=\"%s\"}\n" +
                "  runtimes = [%s]\n" +
                "  deployment_roles = [%s]\n" +
                "}\n", deploymentId, functionNames, paths, handlers, timeouts, memorySizes, layers, layerRootPath,
            runtimes, deploymentRoles
        );
    }
}
