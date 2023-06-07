package at.uibk.dps.rm.entity.deployment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Getter
public class LambdaDeploymentData {
    private String functionNames = "";
    private String paths = "";
    private String handlers = "";
    private String timeouts = "";
    private String memorySizes = "";
    private String layers = "";
    private String runtimes = "";

    @Setter
    @Getter
    private String awsRole = "";

    @Getter
    private long functionCount = 0;

    public void appendValues(String functionName, String path, String handler,
        BigDecimal timeout, BigDecimal memorySize, String layer, String runtime) {
        this.functionNames += addQuotes(functionName);
        this.paths += addQuotes(path);
        this.handlers += addQuotes(handler);
        this.timeouts += timeout + ",";
        this.memorySizes += memorySize + ",";
        this.layers += layer + ",";
        this.runtimes += addQuotes(runtime);
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
                "  names = [%s]\n" +
                "  paths = [%s]\n" +
                "  handlers = [%s]\n" +
                "  timeouts = [%s]\n" +
                "  memory_sizes = [%s]\n" +
                "  layers = [%s]\n" +
                "  runtimes = [%s]\n" +
                "  aws_role = \"%s\"\n" +
                "}\n", functionNames, paths, handlers, timeouts,
            memorySizes, layers, runtimes, awsRole
        );
    }
}
