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
        K8sClusterVmQueryProvider ks8ClusterQueryProvider = new K8sClusterVmQueryProvider(resourceFilter);
        K8sNodeVmQueryProvider k8sNodeQueryProvider = new K8sNodeVmQueryProvider(resourceFilter);
        NodeVmQueryProvider nodeQueryProvider = new NodeVmQueryProvider(resourceFilter, noDeploymentFilter,
            mountPointFilter, fsTypeFilter);
        RegionVmQueryProvider regionQueryProvider = new RegionVmQueryProvider(regionFilter, platformIds,
            instanceTypeResources.keySet());
        boolean includeSubResources = false;
        double stepMinutes = 5;
        switch (metricEnum) {
            case AVAILABILITY:
                // K8s Cluster, K8s Node
                includeSubResources = true;
                VmQuery k8sAvailability = ks8ClusterQueryProvider.getAvailability();
                // Node Exporter
                VmQuery nodeAvailability = nodeQueryProvider.getAvailability();
                // Lambda, EC2
                VmQuery regionAvailability = regionQueryProvider.getAvailability();

                metricQueryObservable = Observable.fromArray(k8sAvailability, nodeAvailability, regionAvailability);
                break;
            case COST:
                // Lambda, EC2
                VmQuery awsPrice = regionQueryProvider.getCost();

                VmConditionQuery awsPriceQuery = new VmConditionQuery(awsPrice, slo.getValue(), slo.getExpression(),
                    config.getAwsPriceMonitoringPeriod() + 60);

                stepMinutes = config.getAwsPriceMonitoringPeriod();
                staticMetricObservable = queryService.collectInstantMetric(awsPriceQuery.toString())
                    .flatMapObservable(Observable::fromIterable)
                    .flatMap(vmResult -> {
                        MonitoredMetricValue monitoredMetricValue = new MonitoredMetricValue(metricEnum);
                        monitoredMetricValue.setValueNumber(vmResult.getValues().get(0).getValue());
                        return Observable.fromIterable(instanceTypeResources.get(vmResult.getMetric().get(
                            "instance_type")))
                            .map(resources::get)
                            .filter(resource -> regionResources.get(vmResult.getMetric().get("region"))
                                .contains(resource.getResourceId().toString()))
                            .map(resource -> {
                                resource.getMonitoredMetricValues().add(monitoredMetricValue);
                                return resource;
                            });
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet())
                    .toObservable();
                break;
            case CPU:
                // K8s Cluster
                VmQuery k8sCpuTotal = ks8ClusterQueryProvider.getCpu();
                // K8s Node
                VmQuery k8sNodeCpuTotal = k8sNodeQueryProvider.getCpu();
                // Node Exporter
                VmQuery nodeCount = nodeQueryProvider.getCpu();
                // EC2
                staticMetricObservable = filterAndMapStaticMetricToEC2Resource(resources, platformResources,
                    metricEnum, slo);

                metricQueryObservable = Observable.fromArray(k8sCpuTotal, k8sNodeCpuTotal, nodeCount);
                break;
            case CPU_UTIL:
                // K8s Cluster
                VmQuery k8sCpuUtil = ks8ClusterQueryProvider.getCpuUtil();
                // K8s Node
                VmQuery k8sNodeCpuUtil = k8sNodeQueryProvider.getCpuUtil();
                // Node Exporter
                VmQuery nodeCpuUtil = nodeQueryProvider.getCpuUtil();

                metricQueryObservable = Observable.fromArray(k8sCpuUtil, k8sNodeCpuUtil, nodeCpuUtil);
                break;
            case LATENCY:
                // Lambda, EC2
                VmQuery regionLatency = regionQueryProvider.getLatency();
                // TODO: Add k8s, openfaas
                includeSubResources = true;

                metricQueryObservable = Observable.fromArray(regionLatency);
                break;
            case MEMORY:
                // K8s Cluster
                VmQuery k8sMemoryTotal = ks8ClusterQueryProvider.getMemory();
                // K8s Node
                VmQuery k8sNodeMemoryTotal = k8sNodeQueryProvider.getMemory();
                // Node Exporter
                VmQuery nodeMemTotal = nodeQueryProvider.getMemory();
                // EC2
                staticMetricObservable = filterAndMapStaticMetricToEC2Resource(resources, platformResources,
                    metricEnum, slo);

                metricQueryObservable = Observable.fromArray(k8sMemoryTotal, k8sNodeMemoryTotal, nodeMemTotal);
                break;
            case MEMORY_UTIL:
                // K8s Cluster
                VmQuery k8sMemoryUtil = ks8ClusterQueryProvider.getMemoryUtil();
                // K8s Node
                VmQuery k8sNodeMemoryUtil = k8sNodeQueryProvider.getMemoryUtil();
                // Node Exporter
                VmQuery nodeMemUtil = nodeQueryProvider.getMemoryUtil();

                metricQueryObservable = Observable.fromArray(k8sMemoryUtil, k8sNodeMemoryUtil, nodeMemUtil);
                break;
            case NODE:
                // K8s Node
                Set<String> nodeNames = slo.getValue().stream()
                    .map(SLOValue::getValueString)
                    .collect(Collectors.toSet());
                VmQuery k8sNode = k8sNodeQueryProvider.getResourcesByNodeName(nodeNames);

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
                VmQuery nodeStorageTotal = nodeQueryProvider.getStorage();
                // EC2
                staticMetricObservable = filterAndMapStaticMetricToEC2Resource(resources, platformResources,
                    metricEnum, slo);

                metricQueryObservable = Observable.fromArray(nodeStorageTotal);
                break;
            case STORAGE_UTIL:
                // Node Exporter
                VmQuery nodeStoragePercent = nodeQueryProvider.getStorageUtil();
                metricQueryObservable = Observable.fromArray(nodeStoragePercent);
                break;
            case UP:
                // K8s Cluster, K8s Node
                includeSubResources = true;
                VmQuery k8sUp = ks8ClusterQueryProvider.getUp();
                // Node Exporter
                VmQuery nodeUp = nodeQueryProvider.getUp();
                // Lambda, EC2
                VmQuery regionUp = regionQueryProvider.getUp();

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
