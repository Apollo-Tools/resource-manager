package at.uibk.dps.rm.util.monitoring;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.entity.model.MainResource;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import at.uibk.dps.rm.entity.monitoring.victoriametrics.*;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used to filter resources by monitored metrics.
 *
 * @author matthi-g
 */
public class ResourceFilterProvider {

    private final MetricQueryService queryService;

    private final ConfigDTO config;

    private final Map<String, Resource> resources;

    private final HashSetValuedHashMap<String, String> regionResources;

    private final HashSetValuedHashMap<Pair<String, String>, String> platformResources;

    private final HashSetValuedHashMap<String, String> instanceTypeResources;

    private final K8sClusterVmQueryProvider k8sClusterQueryProvider;

    private final K8sNodeVmQueryProvider k8sNodeQueryProvider;

    private final NodeVmQueryProvider nodeQueryProvider;

    private final RegionVmQueryProvider regionQueryProvider;

    public ResourceFilterProvider(MetricQueryService queryService, ConfigDTO config, Map<String, Resource> resources,
            Set<String> resourceIds, Set<String> mainResourceIds, HashSetValuedHashMap<String, String> regionResources,
            HashSetValuedHashMap<Pair<String, String>, String> platformResources,
            HashSetValuedHashMap<String, String> instanceTypeResources) {
        this.queryService = queryService;
        this.config = config;
        this.resources = resources;
        this.regionResources = regionResources;
        this.platformResources = platformResources;
        this.instanceTypeResources = instanceTypeResources;
        VmFilter resourceFilter = new VmFilter("resource", "=~", resourceIds);
        VmFilter mainResourceFilter = new VmFilter("resource", "=~", mainResourceIds);
        VmFilter regionFilter = new VmFilter("region", "=~", regionResources.keySet());
        VmFilter noDeploymentFilter = new VmFilter("deployment", "=~", Set.of("-1|"));
        VmFilter mountPointFilter = new VmFilter("mountpoint", "=", Set.of("/"));
        VmFilter fsTypeFilter = new VmFilter("fstype", "!=", Set.of("rootfs"));
        Set<String> platformIds = platformResources.keySet().stream()
            .map(Pair::getKey)
            .collect(Collectors.toSet());
        k8sClusterQueryProvider = new K8sClusterVmQueryProvider(resourceFilter,
            mainResourceFilter);
        k8sNodeQueryProvider = new K8sNodeVmQueryProvider(resourceFilter);
        nodeQueryProvider = new NodeVmQueryProvider(resourceFilter, noDeploymentFilter,
            mountPointFilter, fsTypeFilter);
        regionQueryProvider = new RegionVmQueryProvider(regionFilter, platformIds,
            instanceTypeResources.keySet());
    }

    /**
     * Filter resources from the external monitoring system based on a service level objective
     *
     * @param metricEnum the metric that corresponds to the service level objective
     * @param slo the service level objective
     * @return a Single that emits a Set of the filtered resources
     */
    public Single<Set<Resource>> filterResourcesByMonitoredMetrics(MonitoringMetricEnum metricEnum,
            ServiceLevelObjective slo) {
        boolean includeSubResources = metricEnum.equals(MonitoringMetricEnum.AVAILABILITY) ||
            metricEnum.equals(MonitoringMetricEnum.LATENCY) || metricEnum.equals(MonitoringMetricEnum.UP);
        double stepMinutes = metricEnum == MonitoringMetricEnum.COST ? config.getAwsPriceMonitoringPeriod() : 5;
        Observable<VmQuery> metricQueryObservable = getDynamicMetricQuery(metricEnum);
        Observable<Set<Resource>> staticMetricObservable = getStaticMetricQuery(metricEnum, slo, resources,
            platformResources, instanceTypeResources, regionResources);
        return Observable.merge(metricQueryObservable
                .map(query -> new VmConditionQuery(query, slo.getValue(), slo.getExpression()))
                .flatMapSingle(query -> queryService.collectInstantMetric(query.toString(), stepMinutes))
                .flatMapSingle(vmResults -> mapMonitoredMetricToResources(metricEnum, resources, regionResources,
                    vmResults, includeSubResources)),
                staticMetricObservable)
            .reduce((currSet, nextSet) -> {
                currSet.addAll(nextSet);
                return currSet;
            })
            .switchIfEmpty(Single.just(new HashSet<>()));
    }

    public Completable queryMetricValuesForResourcesBySLOs(List<EnsembleSLO> ensembleSLOs) {
        return Observable.fromIterable(ensembleSLOs)
            .map(MonitoringMetricEnum::fromEnsembleSLO)
            .flatMap(metricEnum -> {
                boolean includeSubResources = metricEnum.equals(MonitoringMetricEnum.AVAILABILITY) ||
                    metricEnum.equals(MonitoringMetricEnum.LATENCY) || metricEnum.equals(MonitoringMetricEnum.UP);
                double stepMinutes = metricEnum == MonitoringMetricEnum.COST ? config.getAwsPriceMonitoringPeriod() : 5;
                return getDynamicMetricQuery(metricEnum)
                    .flatMapSingle(vmQuery -> queryService.collectInstantMetric(vmQuery.toString(), stepMinutes))
                    .flatMapSingle(vmResults -> mapMonitoredMetricToResources(metricEnum, resources, regionResources,
                        vmResults, includeSubResources));
            })
            .ignoreElements();

    }

    private Observable<VmQuery> getDynamicMetricQuery(MonitoringMetricEnum metricEnum) {
        switch (metricEnum) {
            case AVAILABILITY:
                // K8s Cluster, K8s Node
                VmQuery k8sAvailability = k8sClusterQueryProvider.getAvailability();
                // Node Exporter
                VmQuery nodeAvailability = nodeQueryProvider.getAvailability();
                // Lambda, EC2
                VmQuery regionAvailability = regionQueryProvider.getAvailability();

                return Observable.fromArray(k8sAvailability, nodeAvailability, regionAvailability);
            case CPU:
                // K8s Cluster
                VmQuery k8sCpuTotal = k8sClusterQueryProvider.getCpu();
                // K8s Node
                VmQuery k8sNodeCpuTotal = k8sNodeQueryProvider.getCpu();
                // Node Exporter
                VmQuery nodeCount = nodeQueryProvider.getCpu();

                return Observable.fromArray(k8sCpuTotal, k8sNodeCpuTotal, nodeCount);
            case CPU_UTIL:
                // K8s Cluster
                VmQuery k8sCpuUtil = k8sClusterQueryProvider.getCpuUtil();
                // K8s Node
                VmQuery k8sNodeCpuUtil = k8sNodeQueryProvider.getCpuUtil();
                // Node Exporter
                VmQuery nodeCpuUtil = nodeQueryProvider.getCpuUtil();

                return Observable.fromArray(k8sCpuUtil, k8sNodeCpuUtil, nodeCpuUtil);
            case LATENCY:
                // Lambda, EC2
                VmQuery regionLatency = regionQueryProvider.getLatency();
                // K8s
                VmQuery k8sLatency = k8sClusterQueryProvider.getLatency();
                // OpenFaas
                VmQuery openfaasLatency = nodeQueryProvider.getLatency();

                return Observable.fromArray(regionLatency, k8sLatency, openfaasLatency);
            case MEMORY:
                // K8s Cluster
                VmQuery k8sMemoryTotal = k8sClusterQueryProvider.getMemory();
                // K8s Node
                VmQuery k8sNodeMemoryTotal = k8sNodeQueryProvider.getMemory();
                // Node Exporter
                VmQuery nodeMemTotal = nodeQueryProvider.getMemory();

                return Observable.fromArray(k8sMemoryTotal, k8sNodeMemoryTotal, nodeMemTotal);
            case MEMORY_UTIL:
                // K8s Cluster
                VmQuery k8sMemoryUtil = k8sClusterQueryProvider.getMemoryUtil();
                // K8s Node
                VmQuery k8sNodeMemoryUtil = k8sNodeQueryProvider.getMemoryUtil();
                // Node Exporter
                VmQuery nodeMemUtil = nodeQueryProvider.getMemoryUtil();

                return Observable.fromArray(k8sMemoryUtil, k8sNodeMemoryUtil, nodeMemUtil);
            case STORAGE:
                // Node Exporter
                VmQuery nodeStorageTotal = nodeQueryProvider.getStorage();

                return Observable.fromArray(nodeStorageTotal);
            case STORAGE_UTIL:
                // Node Exporter
                VmQuery nodeStoragePercent = nodeQueryProvider.getStorageUtil();
                return Observable.fromArray(nodeStoragePercent);
            case UP:
                // K8s Cluster, K8s Node
                VmQuery k8sUp = k8sClusterQueryProvider.getUp();
                // Node Exporter
                VmQuery nodeUp = nodeQueryProvider.getUp();
                // Lambda, EC2
                VmQuery regionUp = regionQueryProvider.getUp();

                return Observable.fromArray(k8sUp, nodeUp, regionUp);
        }
        return Observable.empty();
    }

    private Observable<Set<Resource>> getStaticMetricQuery(MonitoringMetricEnum metricEnum, ServiceLevelObjective slo,
            Map<String, Resource> resources, HashSetValuedHashMap<Pair<String, String>, String> platformResources,
            HashSetValuedHashMap<String, String> instanceTypeResources,
            HashSetValuedHashMap<String, String> regionResources) {
        switch (metricEnum) {
            case COST:
                // TODO: add cost for k8s and openfaas
                // Lambda, EC2
                VmQuery awsPrice = regionQueryProvider.getCost();

                VmConditionQuery awsPriceQuery = new VmConditionQuery(awsPrice, slo.getValue(), slo.getExpression());

                return queryService.collectInstantMetric(awsPriceQuery.toString(), config.getAwsPriceMonitoringPeriod() + 60)
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
            case CPU:
            case MEMORY:
            case STORAGE:
                // EC2
                return filterAndMapStaticMetricToEC2Resource(resources, platformResources,
                    metricEnum, slo);
            case NODE:
                // K8s Node
                Set<String> nodeNames = slo.getValue().stream()
                    .map(SLOValue::getValueString)
                    .collect(Collectors.toSet());
                VmQuery k8sNode = k8sNodeQueryProvider.getResourcesByNodeName(nodeNames);

                return queryService.collectInstantMetric(k8sNode.toString(), 5)
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
        }
        return Observable.empty();
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
                    String resourceId = vmResult.getMetric().get("resource");
                    Resource resource = resources.get(resourceId);
                    if (resource == null) {
                        resource = new MainResource();
                        resource.setResourceId(Long.valueOf(resourceId));
                    }
                    Set<Resource> resourceSet = new HashSet<>();
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
            })
            .flatMapObservable(Observable::fromIterable)
            .filter(resource -> resources.containsKey(resource.getResourceId().toString()))
            .collect(Collectors.toSet());
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
