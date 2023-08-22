package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.entity.monitoring.K8sNode;
import io.kubernetes.client.custom.Quantity;
import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can be used to parse the output of the kubectl command "kube describe node".
 *
 * @author matthi-g
 */
@UtilityClass
public class K8sDescribeParser {
    /**
     * Parse describe block of node.
     *
     * @param describeBlock the describe block
     * @param node the node
     */
    public static void parseContent(String describeBlock, K8sNode node) {
        String allocationPattern = "^\\s*(cpu|memory|ephemeral-storage)\\s+(\\d+[A-Za-z]*)\\s+\\(\\d+%\\)";
        Pattern pattern = Pattern.compile(allocationPattern, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(describeBlock);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            Quantity value = Quantity.fromString(matcher.group(2).trim());
            switch (key) {
                case "cpu":
                    node.setCpuLoad(value);
                    break;
                case "memory":
                    node.setMemoryLoad(value);
                    break;
                case "ephemeral-storage":
                default:
                    node.setStorageLoad(value);
                    break;
            }
        }
    }


}