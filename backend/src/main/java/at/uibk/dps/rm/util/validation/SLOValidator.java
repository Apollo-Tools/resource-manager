package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.dto.deployment.DeploymentAlertingDTO;
import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.dto.resource.SubResourceDTO;
import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValueType;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import at.uibk.dps.rm.service.rxjava3.monitoring.metricquery.MetricQueryService;
import at.uibk.dps.rm.util.misc.MetricValueMapper;
import at.uibk.dps.rm.util.monitoring.ResourceFilterProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides methods to filter and sort resource by service level objectives.
 *
 * @author matthi-g
 */
public class SLOValidator {

    private final MetricQueryService metricQueryService;

    private final ConfigDTO configDTO;

    private final HashMap<String, Resource> resourceMap = new HashMap<>();
    private final Set<String> resourceIds = new HashSet<>();
    private final Set<String> mainResourceIds = new HashSet<>();
    private final HashSetValuedHashMap<String, String> regionResources = new HashSetValuedHashMap<>();
    private final HashSetValuedHashMap<Pair<String, String>, String> platformResources = new HashSetValuedHashMap<>();
    private final HashSetValuedHashMap<String, String> instanceTypeResources = new HashSetValuedHashMap<>();

    /**
     * Create a new instance from the metricQueryService, configDTO and filterResources.
     *
     * @param metricQueryService the metric query service
     * @param configDTO the vertx config
     * @param filterResources the filtered resources
     */
    public SLOValidator(MetricQueryService metricQueryService, ConfigDTO configDTO, List<Resource> filterResources) {
        this.metricQueryService = metricQueryService;
        this.configDTO = configDTO;
        filterResources.forEach(resource -> {
            resourceIds.add(String.valueOf(resource.getResourceId()));
            resourceMap.put(String.valueOf(resource.getResourceId()), resource);
            Map<String, MetricValue> metricValues =
                MetricValueMapper.mapMetricValues(resource.getMetricValues());
            if (metricValues.containsKey("instance-type")) {
                instanceTypeResources.put(metricValues.get("instance-type").getValueString(),
                    resource.getResourceId().toString());
            }
            if (resource instanceof SubResourceDTO) {
                SubResourceDTO subResourceDTO = (SubResourceDTO) resource;
                regionResources.put(subResourceDTO.getRegion().getRegionId().toString(),
                    subResourceDTO.getResourceId().toString());
                Platform platform = subResourceDTO.getPlatform();
                platformResources.put(new ImmutablePair<>(platform.getPlatformId().toString(),
                    platform.getPlatform()), subResourceDTO.getResourceId().toString());
                if (subResourceDTO.getPlatform().getPlatform().equals(PlatformEnum.LAMBDA.getValue())) {
                    instanceTypeResources.put(PlatformEnum.LAMBDA.getValue(),
                        subResourceDTO.getResourceId().toString());
                } else if (subResourceDTO.getPlatform().getPlatform().equals(PlatformEnum.K8S.getValue())) {
                    mainResourceIds.add(String.valueOf(subResourceDTO.getMainResourceId()));
                }
            } else {
                regionResources.put(resource.getMain().getRegion().getRegionId().toString(),
                    resource.getResourceId().toString());
                Platform platform = resource.getMain().getPlatform();
                platformResources.put(new ImmutablePair<>(platform.getPlatformId().toString(),
                    platform.getPlatform()), resource.getResourceId().toString());
                if (resource.getMain().getPlatform().getPlatform().equals(PlatformEnum.LAMBDA.getValue())) {
                    instanceTypeResources.put(PlatformEnum.LAMBDA.getValue(),
                        resource.getResourceId().toString());
                }
                mainResourceIds.add(String.valueOf(resource.getResourceId()));
            }
        });
    }

    /**
     * Filter resources by monitored metrics by a list of service level objectives.
     *
     * @param serviceLevelObjectives the service level objectives
     * @return the filtered resources
     */
    public Single<Set<Resource>> filterResourcesByMonitoredMetrics(List<ServiceLevelObjective> serviceLevelObjectives) {
        ResourceFilterProvider queryProvider = new ResourceFilterProvider(metricQueryService, configDTO, resourceMap,
            resourceIds, mainResourceIds, regionResources, platformResources, instanceTypeResources);
        return Observable.fromIterable(serviceLevelObjectives)
            .filter(slo -> MonitoringMetricEnum.fromSLO(slo) != null)
            .toList()
            .flatMap(monitoredSLOs -> {
                if (monitoredSLOs.isEmpty()) {
                    return Single.just(new HashSet<>(resourceMap.values()));
                } else {
                    return Observable.fromIterable(monitoredSLOs)
                        .flatMapSingle(slo -> {
                            MonitoringMetricEnum metric = MonitoringMetricEnum.fromSLO(slo);
                            return queryProvider.filterResourcesByMonitoredMetrics(metric, slo);
                        })
                        .reduce((currSet, nextSet) -> {
                            currSet.retainAll(nextSet);
                            return currSet;
                        })
                        .switchIfEmpty(Single.just(Set.of()));
                }
            });
    }

    /**
     * Validate resources by monitored metrics using ensemble SLOs and return invalid resources.
     *
     * @return the invalid resources
     */
    public Single<List<Resource>> validateResourcesByMonitoredMetrics(DeploymentAlertingDTO deployment) {
        ResourceFilterProvider resourceFilterProvider = new ResourceFilterProvider(metricQueryService, configDTO,
            resourceMap, resourceIds, mainResourceIds, regionResources, platformResources, instanceTypeResources);

        return resourceFilterProvider.queryMetricValuesForResourcesBySLOs(deployment.getEnsembleSLOs())
            .andThen(Single.defer(() -> Single.just(deployment.getResources())))
            .flatMapObservable(Observable::fromIterable)
            .flatMapSingle(resource -> Observable.fromIterable(deployment.getEnsembleSLOs())
                .flatMap(ensembleSLO -> Observable.fromIterable(resource.getMonitoredMetricValues())
                    .filter(monitoredMetricValue -> monitoredMetricValue.getMetric().equals(ensembleSLO.getName()))
                    .map(monitoredMetricValue -> {
                        MetricValue metricValue = new MetricValue();
                        metricValue.setValue(monitoredMetricValue.getValueBool());
                        metricValue.setValue(monitoredMetricValue.getValueNumber());
                        metricValue.setValue(monitoredMetricValue.getValueString());
                        ServiceLevelObjective slo = new ServiceLevelObjective(ensembleSLO);
                        monitoredMetricValue
                            .setFulfillsSLO(SLOCompareUtility.compareMetricValueWithSLO(metricValue, slo));
                        return monitoredMetricValue;
                    })
                    .filter(monitoredMetricValue -> !monitoredMetricValue.getFulfillsSLO())
                )
                .collect(Collectors.toSet())
                .map(invalidMetricValues -> {
                    resource.setMonitoredMetricValues(invalidMetricValues);
                    return resource;
                })
            )
            .filter(resource -> !resource.getMonitoredMetricValues().isEmpty())
            .toList();
    }

    /**
     * The sorting condition for resources based on the serviceLevelObjectives
     *
     * @param r1 the first resource to compare
     * @param r2 the second resource to compare
     * @param serviceLevelObjectives the service level objectives
     * @return a positive value if r1 should be ranked higher than r2 else a negative value
     */
    public int sortResourceBySLOs(Resource r1, Resource r2, List<ServiceLevelObjective> serviceLevelObjectives) {
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
