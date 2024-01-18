package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import at.uibk.dps.rm.entity.monitoring.victoriametrics.*;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class MetricQueryProvider {

    public static List<VmQuery> getMetricQuery(MonitoringMetricEnum metricEnum, ServiceLevelObjective slo,
                                               Set<String> resourceIds) {
        switch (metricEnum) {
            case CPU_UTIL:
                // K8s Cluster
                VmSingleQuery k8sCpuTotal = new VmSingleQuery("k8s_cpu_total")
                    .setFilter(Map.of("resource", resourceIds));
                VmSingleQuery k8sCpuUsed = new VmSingleQuery("k8s_cpu_used")
                    .setFilter(Map.of("resource", resourceIds));
                VmCompoundQuery k8sCpuUtil = new VmCompoundQuery(k8sCpuUsed, k8sCpuTotal, '/')
                    .setMultiplier(100);
                // K8s Cluster
                VmSingleQuery k8sNodeCpuTotal = new VmSingleQuery("k8s_node_cpu_total")
                    .setFilter(Map.of("resource", resourceIds));
                VmSingleQuery k8sNodeCpuUsed = new VmSingleQuery("k8s_node_cpu_used")
                    .setFilter(Map.of("resource", resourceIds));
                VmCompoundQuery k8sNodeCpuUtil = new VmCompoundQuery(k8sNodeCpuUsed, k8sNodeCpuTotal, '/')
                    .setMultiplier(100);
                // Node Exporter
                VmSingleQuery nodeCpuSecondsTotal = new VmSingleQuery("node_cpu_seconds_total")
                    .setFilter(Map.of("resource", resourceIds, "mode", Set.of("idle"), "deployment", Set.of("-1|")))
                    .setTimeRange("5s");
                VmFunctionQuery nodeRate = new VmFunctionQuery("rate", nodeCpuSecondsTotal);
                VmFunctionQuery nodeAvg = new VmFunctionQuery("avg", nodeRate)
                    .setGroupBy(Set.of("resource", "instance"))
                    .setSummand(-1.0);
                VmSimpleQuery nodeCpuUtil = new VmSimpleQuery(nodeAvg)
                    .setMultiplier(-100);

                return Stream.of(k8sCpuUtil, k8sNodeCpuUtil, nodeCpuUtil)
                    .map(query -> new VmConditionQuery(query, slo.getValue(), slo.getExpression()))
                    .collect(Collectors.toList());
        }
        return null;
    }

}
