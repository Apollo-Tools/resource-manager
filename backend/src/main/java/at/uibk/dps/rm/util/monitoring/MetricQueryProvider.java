package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import at.uibk.dps.rm.entity.monitoring.victoriametrics.*;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MetricQueryProvider {

    private final MetricQueryService queryService;


    public Single<Set<Resource>> getMetricQuery(ConfigDTO config, MonitoringMetricEnum metricEnum,
            ServiceLevelObjective slo, Map<String, Resource> resources, Set<String> resourceIds,
            HashSetValuedHashMap<String, String> regionResources,
            HashSetValuedHashMap<Pair<String, String>, String> platformResources,
            HashSetValuedHashMap<String, String> instanceTypeResources) {
        VmFilter resourceFilter = new VmFilter("resource", "=~", resourceIds);
        VmFilter regionFilter = new VmFilter("region", "=~", regionResources.keySet());
        VmFilter noDeploymentFilter = new VmFilter("deployment", "=~", Set.of("-1|"));
        VmFilter mountPointFilter = new VmFilter("mountpoint", "=", Set.of("/"));
        VmFilter fsTypeFilter = new VmFilter("fstype", "!=", Set.of("rootfs"));
        Observable<VmQuery> metricQueryObservable = Observable.fromArray();
        Observable<Set<Resource>> staticMetricObservable = Observable.fromArray();
        Set<String> platformIds = platformResources.keySet().stream()
            .map(Pair::getKey)
            .collect(Collectors.toSet());
        K8sClusterVmQueryProvider clusterQueryProvider = new K8sClusterVmQueryProvider(resourceFilter);
        K8sNodeVmQueryProvider nodeQueryProvider = new K8sNodeVmQueryProvider(resourceFilter);
        boolean includeSubResources = false;
        double stepMinutes = 5;
        switch (metricEnum) {
            case AVAILABILITY:
                // K8s Cluster
                // K8s Node
                includeSubResources = true;
                VmQuery k8sAvailability = clusterQueryProvider.getAvailability();
                // Node Exporter
                VmSingleQuery nodeUpRange = new VmSingleQuery("up")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter))
                    .setTimeRange("30d");
                VmFunctionQuery nodeAvailability = new VmFunctionQuery("avg_over_time", nodeUpRange)
                    .setMultiplier(100.0);
                // lambda
                // ec2
                VmSingleQuery regionUpRange = new VmSingleQuery("region_up")
                    .setFilter(Set.of(regionFilter))
                    .setTimeRange("30d");
                VmFunctionQuery regionAvailability = new VmFunctionQuery("avg_over_time", regionUpRange)
                    .setMultiplier(100.0);

                metricQueryObservable = Observable.fromArray(k8sAvailability, nodeAvailability, regionAvailability);
                break;
            case COST:
                // lambda
                // ec2
                VmSingleQuery awsPrice = new VmSingleQuery("aws_price_usd")
                    .setFilter(Set.of(regionFilter, new VmFilter("platform", "=~", platformIds),
                        new VmFilter("instance_type", "=~", instanceTypeResources.keySet())));

                stepMinutes = config.getAwsPriceMonitoringPeriod();
                metricQueryObservable = Observable.fromArray(awsPrice);
                break;
            case CPU:
                // K8s Cluster
                VmQuery k8sCpuTotal = clusterQueryProvider.getCpu();
                // K8s Node
                VmQuery k8sNodeCpuTotal = nodeQueryProvider.getCpu();
                // Node Exporter
                VmSingleQuery nodeCpuSecondsTotal = new VmSingleQuery("node_cpu_seconds_total")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter,
                        new VmFilter("mode", "=", Set.of("system"))));
                VmFunctionQuery nodeCount = new VmFunctionQuery("count", nodeCpuSecondsTotal)
                    .setGroupBy(Set.of("resource", "instance"));
                // EC 2
                staticMetricObservable = filterAndMapStaticMetricToEC2Resource(resources, platformResources,
                    metricEnum, slo);

                metricQueryObservable = Observable.fromArray(k8sCpuTotal, k8sNodeCpuTotal, nodeCount);
                break;
            case CPU_UTIL:
                // K8s Cluster
                VmQuery k8sCpuUtil = clusterQueryProvider.getCpuUtil();
                // K8s Node
                VmQuery k8sNodeCpuUtil = nodeQueryProvider.getCpuUtil();
                // Node Exporter
                VmSingleQuery nodeCpuSecondsTotalUtil = new VmSingleQuery("node_cpu_seconds_total")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter,
                        new VmFilter("mode", "=", Set.of("idle"))))
                    .setTimeRange("5s");
                VmFunctionQuery nodeRate = new VmFunctionQuery("rate", nodeCpuSecondsTotalUtil);
                VmFunctionQuery nodeAvg = new VmFunctionQuery("avg", nodeRate)
                    .setGroupBy(Set.of("resource", "instance"))
                    .setSummand(-1.0);
                VmSimpleQuery nodeCpuUtil = new VmSimpleQuery(nodeAvg)
                    .setMultiplier(-100);

                metricQueryObservable = Observable.fromArray(k8sCpuUtil, k8sNodeCpuUtil, nodeCpuUtil);
                break;
            case LATENCY:
                // lambda, ec2
                VmSingleQuery regionLatency = new VmSingleQuery("region_latency_seconds")
                    .setFilter(Set.of(regionFilter));
                // TODO: Add k8s, openfaas
                includeSubResources = true;

                metricQueryObservable = Observable.fromArray(regionLatency);
                break;
            case MEMORY:
                // K8s Cluster
                VmQuery k8sMemoryTotal = clusterQueryProvider.getMemory();
                // K8s Node
                VmQuery k8sNodeMemoryTotal = nodeQueryProvider.getMemory();
                // Node Exporter
                VmSingleQuery nodeMemTotal = new VmSingleQuery("node_memory_MemTotal_bytes")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter));
                // EC 2
                staticMetricObservable = filterAndMapStaticMetricToEC2Resource(resources, platformResources,
                    metricEnum, slo);

                metricQueryObservable = Observable.fromArray(k8sMemoryTotal, k8sNodeMemoryTotal, nodeMemTotal);
                break;
            case MEMORY_UTIL:
                // K8s Cluster
                VmQuery k8sMemoryUtil = clusterQueryProvider.getMemoryUtil();
                // K8s Node
                VmQuery k8sNodeMemoryUtil = nodeQueryProvider.getMemoryUtil();
                // Node Exporter
                VmSingleQuery nodeMemFree = new VmSingleQuery("node_memory_MemFree_bytes")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter));
                VmSingleQuery nodeMemBuffer = new VmSingleQuery("node_memory_Buffers_bytes")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter));
                VmSingleQuery nodeMemCached = new VmSingleQuery("node_memory_Cached_bytes")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter));
                VmSingleQuery nodeMemTotalUtil = new VmSingleQuery("node_memory_MemTotal_bytes")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter));
                VmCompoundQuery nodeMemFreeBuffer = new VmCompoundQuery(nodeMemFree, nodeMemBuffer, '+');
                VmCompoundQuery nodeMemUsedTotal = new VmCompoundQuery(nodeMemFreeBuffer, nodeMemCached, '+');
                VmCompoundQuery nodeMemUtilAbsolute = new VmCompoundQuery(nodeMemUsedTotal, nodeMemTotalUtil, '/')
                    .setSummand(-1);
                VmSimpleQuery nodeMemUtil = new VmSimpleQuery(nodeMemUtilAbsolute)
                    .setMultiplier(-100);

                metricQueryObservable = Observable.fromArray(k8sMemoryUtil, k8sNodeMemoryUtil, nodeMemUtil);
                break;
            case NODE:
                // K8s Node
                Set<String> nodeNames = slo.getValue().stream()
                    .map(SLOValue::getValueString)
                    .collect(Collectors.toSet());
                VmQuery k8sNode = nodeQueryProvider.getResourcesByNodeName(nodeNames);

                staticMetricObservable = queryService.collectInstantMetric(k8sNode.toString())
                    .flatMapObservable(Observable::fromIterable)
                    .map(vmResult -> {
                        MonitoredMetricValue monitoredMetricValue = new MonitoredMetricValue(metricEnum);
                        monitoredMetricValue.setValueString(vmResult.getMetric().get("node"));
                        Resource resource = resources.get(vmResult.getMetric().get("resource"));
                        resource.getMonitoredMetricValues().add(monitoredMetricValue);
                        return resource;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
                    .toObservable();
                break;
            case STORAGE:
                // Node Exporter
                VmSingleQuery nodeStorageTotal = new VmSingleQuery("node_filesystem_size_bytes")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter, mountPointFilter, fsTypeFilter));
                // EC 2
                staticMetricObservable = filterAndMapStaticMetricToEC2Resource(resources, platformResources,
                    metricEnum, slo);

                metricQueryObservable = Observable.fromArray(nodeStorageTotal);
                break;
            case STORAGE_UTIL:
                // Node Exporter
                VmSingleQuery nodeStorageAvail = new VmSingleQuery("node_filesystem_avail_bytes")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter, mountPointFilter, fsTypeFilter))
                    .setMultiplier(-100.0);
                VmSingleQuery nodeStorageTotalUtil = new VmSingleQuery("node_filesystem_size_bytes")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter, mountPointFilter, fsTypeFilter));
                VmCompoundQuery nodeStoragePercent = new VmCompoundQuery(nodeStorageAvail, nodeStorageTotalUtil, '/')
                    .setSummand(100);

                metricQueryObservable = Observable.fromArray(nodeStoragePercent);
                break;
            case UP:
                // K8s Cluster
                // K8s Node
                includeSubResources = true;
                VmQuery k8sUp = clusterQueryProvider.getUp();
                // Node Exporter
                VmSingleQuery nodeUp = new VmSingleQuery("up")
                    .setFilter(Set.of(resourceFilter, noDeploymentFilter));
                // lambda
                // ec2
                VmSingleQuery regionUp = new VmSingleQuery("region_up")
                    .setFilter(Set.of(regionFilter));

                metricQueryObservable = Observable.fromArray(k8sUp, nodeUp, regionUp);
                break;
        }
        boolean finalIncludeSubResources = includeSubResources;
        double finalStepMinutes = stepMinutes;
        return Observable.merge(metricQueryObservable
                .map(query -> new VmConditionQuery(query, slo.getValue(), slo.getExpression(), finalStepMinutes))
                .flatMapSingle(query -> queryService.collectInstantMetric(query.toString()))
                .flatMapSingle(vmResults -> mapMonitoredMetricToResources(metricEnum, resources, regionResources,
                    vmResults, finalIncludeSubResources)),
                staticMetricObservable)
            .reduce((currSet, nextSet) -> {
                currSet.addAll(nextSet);
                return currSet;
            })
            .switchIfEmpty(Single.just(new HashSet<>()));
    }

    private Observable<Set<Resource>> filterAndMapStaticMetricToEC2Resource(Map<String, Resource> resources,
            HashSetValuedHashMap<Pair<String, String>, String> platformResources,
            MonitoringMetricEnum metricEnum, ServiceLevelObjective slo) {
        return Observable.fromIterable(platformResources.keySet())
            .filter(platform -> platform.getValue().equals(PlatformEnum.EC2.getValue()))
            .first(new ImmutablePair<>("", ""))
            .flatMapObservable(pair -> Observable.fromIterable(platformResources.get(pair)))
            .map(resources::get)
            .filter(resource -> {
                Map<String, MetricValue> metricValueMap = MetricValueMapper
                    .mapMetricValues(resource.getMetricValues());
                MetricValue metricValue = metricValueMap.get(metricEnum.getName());
                if (metricValue != null && SLOCompareUtility.compareMetricValueWithSLO(metricValue, slo)) {
                    MonitoredMetricValue monitoredMetricValue = new MonitoredMetricValue(metricEnum);
                    monitoredMetricValue.setValueNumber(metricValue.getValueNumber().doubleValue());
                    monitoredMetricValue.setValueString(metricValue.getValueString());
                    monitoredMetricValue.setValueBool(metricValue.getValueBool());
                    resource.getMonitoredMetricValues().add(monitoredMetricValue);
                    return true;
                }
                return false;
            })
            .collect(Collectors.toSet())
            .toObservable();
    }

    private Single<Set<Resource>> mapMonitoredMetricToResources(MonitoringMetricEnum metricEnum,
            Map<String, Resource> resources, HashSetValuedHashMap<String, String> regionResources,
            List<VmResult> vmResults, boolean includeSubResources) {
        return Observable.fromIterable(vmResults)
            .map(vmResult -> {
                MonitoredMetricValue metricValue = new MonitoredMetricValue(metricEnum);
                metricValue.setValueNumber(vmResult.getValues().get(0).getValue());
                if (vmResult.getMetric().containsKey("resource")) {
                    Set<Resource> resourceSet = new HashSet<>();
                    Resource resource = resources.get(vmResult.getMetric().get("resource"));
                    resource.getMonitoredMetricValues().add(metricValue);
                    resourceSet.add(resource);
                    return resourceSet;
                }
                return regionResources.get(vmResult.getMetric().get("region"))
                    .stream()
                    .map(resources::get)
                    .peek(resource -> resource.getMonitoredMetricValues().add(metricValue))
                    .collect(Collectors.toSet());
            })
            .collectInto(new HashSet<Resource>(), Set::addAll)
            .flatMap(updatedResources -> {
                if (!includeSubResources) {
                    return Single.just(updatedResources);
                }
                return addSubResourcesToFilteredResources(updatedResources, resources, metricEnum);
            });
    }

    private Single<Set<Resource>> addSubResourcesToFilteredResources(Set<Resource> updatedResources,
            Map<String, Resource> resources, MonitoringMetricEnum metricEnum) {
        return Observable.fromIterable(updatedResources)
            .flatMapSingle(mainResource -> Observable.fromIterable(resources.values())
                .filter(resource -> {
                    if (!(resource instanceof SubResourceDTO)) {
                        return false;
                    }
                    SubResourceDTO subResource = (SubResourceDTO) resource;
                    if (subResource.getMainResourceId() != mainResource.getResourceId()) {
                        return false;
                    }
                    Map<String, MonitoredMetricValue> metricValueMap = MetricValueMapper
                        .mapMonitoredMetricValues(mainResource.getMonitoredMetricValues());
                    subResource.getMonitoredMetricValues().add(metricValueMap.get(metricEnum.getName()));
                    return true;
                })
                .collect(Collectors.toSet())
            )
            .reduce(new HashSet<>(updatedResources), (currSet, nextSet) -> {
                currSet.addAll(nextSet);
                return currSet;
            });
    }

}
