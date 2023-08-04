package at.uibk.dps.rm.util.misc;

import at.uibk.dps.rm.entity.monitoring.K8sNode;
import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class K8sDescribeParser {

    public static void parseContent(String describeBlock, K8sNode node) {
        String allocationPattern = "^\\s*(cpu|memory|ephemeral-storage)\\s+(\\d+)[A-Za-z]*\\s+\\(\\d+%\\)";
        Pattern pattern = Pattern.compile(allocationPattern, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(describeBlock);

        while (matcher.find()) {
            String key = matcher.group(1).trim();
            String value = matcher.group(2).trim();
            switch (key) {
                case "cpu":
                    node.setCpuLoad(Integer.parseInt(value));
                    break;
                case "memory":
                    node.setMemoryLoad(Integer.parseInt(value));
                    break;
                case "ephemeral-storage":
                default:
                    node.setStorageLoad(Long.parseLong(value));
                    break;
            }
        }
    }


}
