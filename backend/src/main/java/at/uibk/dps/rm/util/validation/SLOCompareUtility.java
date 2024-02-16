package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.*;
import lombok.experimental.UtilityClass;

import java.util.*;

/**
 * This utility class is used to compare service level objectives.
 *
 * @author matthi-g
 */
@UtilityClass
public class SLOCompareUtility {

    /**
     * Compare a metric value with a service level objective.
     *
     * @param metricValue the metric value
     * @param slo the service level objective
     * @return true if the metric value fulfills the service level objective else false
     */
    public static Boolean compareMetricValueWithSLO(MetricValue metricValue, ServiceLevelObjective slo) {
        int compareValue = -1;
        boolean isEqualityCheck = slo.getExpression().equals(ExpressionType.EQ);
        for (SLOValue sloValue : slo.getValue()) {
            switch (sloValue.getSloValueType()) {
                case NUMBER:
                    if (metricValue.getValueNumber() == null) {
                        return false;
                    }
                    compareValue = ExpressionType.compareValues(slo.getExpression(),
                            sloValue.getValueNumber().doubleValue(),
                            metricValue.getValueNumber().doubleValue());
                    break;
                case STRING:
                    if (metricValue.getValueString() == null) {
                        return false;
                    }
                    compareValue = ExpressionType.compareValues(slo.getExpression(),
                            sloValue.getValueString(),
                            metricValue.getValueString());
                    break;
                case BOOLEAN:
                    if (metricValue.getValueBool() == null) {
                        return false;
                    }
                    compareValue = ExpressionType.compareValues(slo.getExpression(),
                            sloValue.getValueBool(),
                            metricValue.getValueBool());
                    break;
            }
            if (compareValue == 0 && isEqualityCheck) {
                return true;
            }
            else if (compareValue > 0 && !isEqualityCheck) {
                return true;
            }
        }
        return false;
    }

    /**
     * Transform a set of monitored metric values into a list and sort them in the same order
     * as the corresponding service level objectives.
     *
     * @param metricValues the monitored metric values
     * @param serviceLevelObjectives the service level objectives
     * @return a sorte list of monitored metric values
     */
    public static List<MonitoredMetricValue> sortMonitoredMetricValuesBySLOs(Set<MonitoredMetricValue> metricValues,
        List<ServiceLevelObjective> serviceLevelObjectives) {
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < serviceLevelObjectives.size(); i++) {
            indexMap.put(serviceLevelObjectives.get(i).getName(), i);
        }
        List<MonitoredMetricValue> sortedList = new ArrayList<>(metricValues);
        sortedList.sort(Comparator.comparingInt(metricValue -> indexMap
            .getOrDefault(metricValue.getMetric(), indexMap.size())));

        return sortedList;
    }
}
