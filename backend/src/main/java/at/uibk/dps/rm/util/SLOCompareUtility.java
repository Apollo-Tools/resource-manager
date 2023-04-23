package at.uibk.dps.rm.util;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.MetricValue;
import lombok.experimental.UtilityClass;

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
}
