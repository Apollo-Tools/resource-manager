package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import lombok.experimental.UtilityClass;

import java.util.*;
import java.util.stream.Collectors;

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
     * Filter and sort resources based on the service Level Objectives.
     *
     * @param resources the resources
     * @param serviceLevelObjectives the service level objectives used for filtering and sorting
     * @return the filtered and sorted resources as JsonArray
     */
    public static List<Resource> filterAndSortResourcesBySLOs(List<Resource> resources,
            List<ServiceLevelObjective> serviceLevelObjectives) {
        return resources.stream()
            .filter(resource -> resourceFilterBySLOValueType(resource, serviceLevelObjectives))
            .sorted((r1, r2) -> sortResourceBySLOs(r1, r2, serviceLevelObjectives))
            .collect(Collectors.toList());
    }

    /**
     * The filter condition for a resource based on the serviceLevelObjectives.
     *
     * @param resource the resource
     * @param slos the service level objectives
     * @return true if all service level objectives are adhered else false
     */
    public static boolean resourceFilterBySLOValueType(Resource resource, List<ServiceLevelObjective> slos) {
        boolean sloFulfilled;
        for (ServiceLevelObjective slo : slos) {
            sloFulfilled = false;
            for (MetricValue metricValue : resource.getMetricValues()) {
                Metric metric = metricValue.getMetric();
                if (metric.getMetric().equals(slo.getName())) {
                    if (!SLOCompareUtility.compareMetricValueWithSLO(metricValue, slo)) {
                        return false;
                    } else {
                        sloFulfilled = true;
                        break;
                    }
                }
            }
            if (!sloFulfilled) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a resource fulfills the non-metric service level objectives (region,
     * resource provider, resource type) from an ensemble.
     *
     * @param resource the resource
     * @param ensemble the ensemble
     * @return true if the resource fulfills all non-metric service level objectives, else false
     */
    public static boolean resourceValidByNonMetricSLOS(Resource resource, Ensemble ensemble) {
        boolean validRegion = true, validResourceProvider = true, validResourceType = true, validPlatforms = true,
            validEnvironments = true;
        if (!ensemble.getRegions().isEmpty()) {
            validRegion = ensemble.getRegions().contains(resource.getMain().getRegion().getRegionId());
        }
        if (!ensemble.getProviders().isEmpty()) {
            validResourceProvider = ensemble.getProviders()
                .contains(resource.getMain().getRegion().getResourceProvider().getProviderId());
        }
        if (!ensemble.getResource_types().isEmpty()) {
            validResourceType = ensemble.getResource_types()
                .contains(resource.getMain().getPlatform().getResourceType().getTypeId());
        }
        if (!ensemble.getPlatforms().isEmpty()) {
            validPlatforms = ensemble.getPlatforms()
                .contains(resource.getMain().getPlatform().getPlatformId());
        }
        if (!ensemble.getEnvironments().isEmpty()) {
            validEnvironments = ensemble.getEnvironments()
                .contains(resource.getMain().getRegion().getResourceProvider().getEnvironment().getEnvironmentId());
        }
        return validRegion && validResourceProvider && validResourceType && validPlatforms && validEnvironments;
    }

    public static List<MonitoredMetricValue> sortMonitoredMetricValuesBySLOs(Set<MonitoredMetricValue> metricValues,
        List<ServiceLevelObjective> serviceLevelObjectives) {
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < serviceLevelObjectives.size(); i++) {
            indexMap.put(serviceLevelObjectives.get(i).getName(), i);
        }
        List<MonitoredMetricValue> sortedList = new ArrayList<>(metricValues);
        sortedList.sort(Comparator.comparingInt(metricValue -> indexMap.get(metricValue.getMetric())));

        return sortedList;
    }

    /**
     * The sorting condition for resources based on the serviceLevelObjectives
     *
     * @param r1 the first resource to compare
     * @param r2 the second resource to compare
     * @param serviceLevelObjectives the service level objectives
     * @return a positive value if r1 should be ranked higher than r2 else a negative value
     */
    public static int sortResourceBySLOs(Resource r1, Resource r2,
            List<ServiceLevelObjective> serviceLevelObjectives) {
        for (int i = 0; i < serviceLevelObjectives.size(); i++) {
            ServiceLevelObjective slo = serviceLevelObjectives.get(i);
            if (slo.getValue().get(0).getSloValueType() != SLOValueType.NUMBER) {
                continue;
            }
            for (MonitoredMetricValue metricValue1 : r1.getMonitoredMetricValues()) {
                String metric1 = metricValue1.getMetric();
                if (metric1.equals(slo.getName())) {
                    for (MonitoredMetricValue metricValue2 : r2.getMonitoredMetricValues()) {
                        String metric2 = metricValue2.getMetric();
                        if (metric2.equals(slo.getName())) {
                            int compareValue = ExpressionType.compareValues(slo.getExpression(),
                                metricValue1.getValueNumber(),
                                metricValue2.getValueNumber());
                            if (compareValue != 0 || i == serviceLevelObjectives.size() - 1) {
                                return compareValue;
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }
}
