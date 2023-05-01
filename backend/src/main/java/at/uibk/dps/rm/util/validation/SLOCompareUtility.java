package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import lombok.experimental.UtilityClass;

import java.util.List;

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
     * @param metricValue the metric valu
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
     * The filter condition for a resource based on the serviceLevelObjectives.
     *
     * @param resource the resource
     * @param slos the service level objectives
     * @return true if all service level objectives are adhered else false
     */
    public static boolean resourceFilterBySLOValueType(Resource resource, List<ServiceLevelObjective> slos) {
        for (ServiceLevelObjective slo : slos) {
            for (MetricValue metricValue : resource.getMetricValues()) {
                Metric metric = metricValue.getMetric();
                if (metric.getMetric().equals(slo.getName())) {
                    if (!SLOCompareUtility.compareMetricValueWithSLO(metricValue, slo)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
