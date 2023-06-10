package at.uibk.dps.rm.entity.deployment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Getter
public class LambdaDeploymentData {
    private final long deploymentId;
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

    private String addQuotes(String value) {
        return "\"" + value + "\",";
    }

    public String getModuleString() {
        if (functionCount == 0) {
            return "";
        }

        return String.format(
            "module \"lambda\" {\n" +
                "  source = \"../../../terraform/aws/faas\"\n" +
                "  deployment_id = %s\n" +
                "  names = [%s]\n" +
                "  paths = [%s]\n" +
                "  handlers = [%s]\n" +
                "  timeouts = [%s]\n" +
                "  memory_sizes = [%s]\n" +
                "  layers = [%s]\n" +
                "  runtimes = [%s]\n" +
                "  deployment_roles = [%s]\n" +
                "}\n", deploymentId, functionNames, paths, handlers, timeouts,
            memorySizes, layers, runtimes, deploymentRoles
        );
    }
}
