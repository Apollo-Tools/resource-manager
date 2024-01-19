package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import at.uibk.dps.rm.entity.monitoring.victoriametrics.*;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class MetricQueryProvider {

    public static List<VmQuery> getMetricQuery(ConfigDTO config, MonitoringMetricEnum metricEnum,
            ServiceLevelObjective slo, Set<String> resourceIds, HashSetValuedHashMap<String, String> regionResources,
            HashSetValuedHashMap<String, String> platformResources,
            HashSetValuedHashMap<String, String> instanceTypeResources) {
        switch (metricEnum) {
            case AVAILABILITY:
                // K8s Cluster
                // K8s Cluster
                VmSingleQuery k8sUpRange = new VmSingleQuery("k8s_up")
                    .setFilter(Map.of("resource", resourceIds))
                    .setTimeRange("30d");
                VmFunctionQuery k8sAvailability = new VmFunctionQuery("avg_over_time", k8sUpRange);
                // Node Exporter
                VmSingleQuery nodeUpRange = new VmSingleQuery("up")
                    .setFilter(Map.of("resource", resourceIds, "deployment", Set.of("-1|")))
                    .setTimeRange("30d");
                VmFunctionQuery nodeAvailability = new VmFunctionQuery("avg_over_time", nodeUpRange);
                // lambda
                // ec2
                VmSingleQuery regionUpRange = new VmSingleQuery("region_up")
                    .setFilter(Map.of("region", regionResources.keySet()))
                    .setTimeRange("30d");
                VmFunctionQuery regionAvailability = new VmFunctionQuery("avg_over_time", regionUpRange);

                return Stream.of(k8sAvailability, nodeAvailability, regionAvailability)
                    .map(query -> new VmConditionQuery(query, slo.getValue(), slo.getExpression()))
                    .collect(Collectors.toList());
            case LATENCY:
                // lambda, ec2
                VmSingleQuery regionLatency = new VmSingleQuery("region_latency_seconds")
                    .setFilter(Map.of("region", regionResources.keySet()));

                return Stream.of(regionLatency)
                    .map(query -> new VmConditionQuery(query, slo.getValue(), slo.getExpression()))
                    .collect(Collectors.toList());
            case COST:
                // lambda
                // ec2
                VmSingleQuery awsPrice = new VmSingleQuery("aws_price_usd")
                    .setFilter(Map.of("region", regionResources.keySet(), "platform", platformResources.keySet(),
                        "instance_type", instanceTypeResources.keySet()));

                return Stream.of(awsPrice)
                    .map(query -> new VmConditionQuery(query, slo.getValue(), slo.getExpression(),
                        config.getAwsPriceMonitoringPeriod()))
                    .collect(Collectors.toList());
            case CPU:
                // K8s Cluster
                VmSingleQuery k8sCpuTotal = new VmSingleQuery("k8s_cpu_total")
                    .setFilter(Map.of("resource", resourceIds));
                // K8s Cluster
                VmSingleQuery k8sNodeCpuTotal = new VmSingleQuery("k8s_node_cpu_total")
                    .setFilter(Map.of("resource", resourceIds));
                // Node Exporter
                VmSingleQuery nodeCpuSecondsTotal = new VmSingleQuery("node_cpu_seconds_total")
                    .setFilter(Map.of("resource", resourceIds, "mode", Set.of("system"),
                        "deployment", Set.of("-1|")));
                VmFunctionQuery nodeCount = new VmFunctionQuery("count", nodeCpuSecondsTotal)
                    .setGroupBy(Set.of("resource", "instance"));

                return Stream.of(k8sCpuTotal, k8sNodeCpuTotal, nodeCount)
                    .map(query -> new VmConditionQuery(query, slo.getValue(), slo.getExpression()))
                    .collect(Collectors.toList());
            case CPU_UTIL:
                // K8s Cluster
                VmSingleQuery k8sCpuTotalUtil = new VmSingleQuery("k8s_cpu_total")
                    .setFilter(Map.of("resource", resourceIds));
                VmSingleQuery k8sCpuUsed = new VmSingleQuery("k8s_cpu_used")
                    .setFilter(Map.of("resource", resourceIds));
                VmCompoundQuery k8sCpuUtil = new VmCompoundQuery(k8sCpuUsed, k8sCpuTotalUtil, '/')
                    .setMultiplier(100);
                // K8s Cluster
                VmSingleQuery k8sNodeCpuTotalUtil = new VmSingleQuery("k8s_node_cpu_total")
                    .setFilter(Map.of("resource", resourceIds));
                VmSingleQuery k8sNodeCpuUsed = new VmSingleQuery("k8s_node_cpu_used")
                    .setFilter(Map.of("resource", resourceIds));
                VmCompoundQuery k8sNodeCpuUtil = new VmCompoundQuery(k8sNodeCpuUsed, k8sNodeCpuTotalUtil, '/')
                    .setMultiplier(100);
                // Node Exporter
                VmSingleQuery nodeCpuSecondsTotalUtil = new VmSingleQuery("node_cpu_seconds_total")
                    .setFilter(Map.of("resource", resourceIds, "mode", Set.of("idle"), "deployment", Set.of("-1|")))
                    .setTimeRange("5s");
                VmFunctionQuery nodeRate = new VmFunctionQuery("rate", nodeCpuSecondsTotalUtil);
                VmFunctionQuery nodeAvg = new VmFunctionQuery("avg", nodeRate)
                    .setGroupBy(Set.of("resource", "instance"))
                    .setSummand(-1.0);
                VmSimpleQuery nodeCpuUtil = new VmSimpleQuery(nodeAvg)
                    .setMultiplier(-100);

                return Stream.of(k8sCpuUtil, k8sNodeCpuUtil, nodeCpuUtil)
                    .map(query -> new VmConditionQuery(query, slo.getValue(), slo.getExpression()))
                    .collect(Collectors.toList());
            case MEMORY:
                // K8s Cluster
                VmSingleQuery k8sMemoryTotal = new VmSingleQuery("k8s_memory_total_bytes")
                    .setFilter(Map.of("resource", resourceIds));
                // K8s Cluster
                VmSingleQuery k8sNodeMemoryTotal = new VmSingleQuery("k8s_node_memory_total_bytes")
                    .setFilter(Map.of("resource", resourceIds));
                // Node Exporter
                VmSingleQuery nodeMemTotal = new VmSingleQuery("node_memory_MemTotal_bytes")
                    .setFilter(Map.of("resource", resourceIds, "deployment", Set.of("-1|")));

                return Stream.of(k8sMemoryTotal, k8sNodeMemoryTotal, nodeMemTotal)
                    .map(query -> new VmConditionQuery(query, slo.getValue(), slo.getExpression()))
                    .collect(Collectors.toList());
            case MEMORY_UTIL:
                // K8s Cluster
                VmSingleQuery k8sMemoryTotalUtil = new VmSingleQuery("k8s_memory_total_bytes")
                    .setFilter(Map.of("resource", resourceIds));
                VmSingleQuery k8sMemoryUsed = new VmSingleQuery("k8s_memory_used_bytes")
                    .setFilter(Map.of("resource", resourceIds));
                VmCompoundQuery k8sMemoryUtil = new VmCompoundQuery(k8sMemoryUsed, k8sMemoryTotalUtil, '/')
                    .setMultiplier(100);
                // K8s Cluster
                VmSingleQuery k8sNodeMemoryTotalUtil = new VmSingleQuery("k8s_node_memory_total_bytes")
                    .setFilter(Map.of("resource", resourceIds));
                VmSingleQuery k8sNodeMemoryUsed = new VmSingleQuery("k8s_node_memory_used_bytes")
                    .setFilter(Map.of("resource", resourceIds));
                VmCompoundQuery k8sNodeMemoryUtil = new VmCompoundQuery(k8sNodeMemoryUsed, k8sNodeMemoryTotalUtil, '/')
                    .setMultiplier(100);
                // Node Exporter
                VmSingleQuery nodeMemFree = new VmSingleQuery("node_memory_MemFree_bytes")
                    .setFilter(Map.of("resource", resourceIds, "deployment", Set.of("-1|")));
                VmSingleQuery nodeMemBuffer = new VmSingleQuery("node_memory_Buffers_bytes")
                    .setFilter(Map.of("resource", resourceIds, "deployment", Set.of("-1|")));
                VmSingleQuery nodeMemCached = new VmSingleQuery("node_memory_Cached_bytes")
                    .setFilter(Map.of("resource", resourceIds, "deployment", Set.of("-1|")));
                VmSingleQuery nodeMemTotalUtil = new VmSingleQuery("node_memory_MemTotal_bytes")
                    .setFilter(Map.of("resource", resourceIds, "deployment", Set.of("-1|")));
                VmCompoundQuery nodeMemFreeBuffer = new VmCompoundQuery(nodeMemFree, nodeMemBuffer, '+');
                VmCompoundQuery nodeMemUsedTotal = new VmCompoundQuery(nodeMemFreeBuffer, nodeMemCached, '+');
                VmCompoundQuery nodeMemUtilAbsolute = new VmCompoundQuery(nodeMemUsedTotal, nodeMemTotalUtil, '/')
                    .setSummand(-1);
                VmSimpleQuery nodeMemUtil = new VmSimpleQuery(nodeMemUtilAbsolute)
                    .setMultiplier(-100);

                return Stream.of(k8sMemoryUtil, k8sNodeMemoryUtil, nodeMemUtil)
                    .map(query -> new VmConditionQuery(query, slo.getValue(), slo.getExpression()))
                    .collect(Collectors.toList());
            case UP:
                // K8s Cluster
                // K8s Cluster
                VmSingleQuery k8sUp = new VmSingleQuery("k8s_up")
                    .setFilter(Map.of("resource", resourceIds));
                // Node Exporter
                VmSingleQuery nodeUp = new VmSingleQuery("up")
                    .setFilter(Map.of("resource", resourceIds, "deployment", Set.of("-1|")));
                // lambda
                // ec2
                VmSingleQuery regionUp = new VmSingleQuery("region_up")
                    .setFilter(Map.of("region", regionResources.keySet()));


                return Stream.of(k8sUp, nodeUp, regionUp)
                    .map(query -> new VmConditionQuery(query, slo.getValue(), slo.getExpression()))
                    .collect(Collectors.toList());

        }
        return null;
    }

}
