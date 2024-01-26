package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
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
import at.uibk.dps.rm.util.monitoring.MetricQueryProvider;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SLOValidator {

    private final MetricQueryService metricQueryService;

    private final SLORequest sloRequest;

    private final ConfigDTO configDTO;

    public Single<Set<Resource>> filterResourcesByMonitoredMetrics(JsonArray resources) {
        return Observable.fromIterable(resources)
            .map(resource -> ((JsonObject) resource))
            .map(resource -> resource.mapTo(Resource.class))
            .collect(Collectors.toMap(resource -> resource.getResourceId().toString(), resource -> resource))
            .flatMap(filteredResources -> {
                Set<String> resourceIds = filteredResources.keySet();
                Set<String> mainResourceIds = new HashSet<>(resourceIds);
                HashSetValuedHashMap<String, String> regionResources = new HashSetValuedHashMap<>();
                HashSetValuedHashMap<Pair<String, String>, String> platformResources =
                    new HashSetValuedHashMap<>();
                HashSetValuedHashMap<String, String> instanceTypeResources = new HashSetValuedHashMap<>();
                filteredResources.values().forEach(resource -> {
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
                    }
                });
                MetricQueryProvider queryProvider = new MetricQueryProvider(metricQueryService);
                return Observable.fromIterable(sloRequest.getServiceLevelObjectives())
                    .filter(slo -> MonitoringMetricEnum.fromSLO(slo) != null)
                    .toList()
                    .flatMap(monitoredSLOs -> {
                        if (monitoredSLOs.isEmpty()) {
                            return Single.just(new HashSet<>(filteredResources.values()));
                        } else {
                            return Observable.fromIterable(monitoredSLOs)
                                .flatMapSingle(slo -> {
                                    // TODO: fix deployment
                                    MonitoringMetricEnum metric = MonitoringMetricEnum.fromSLO(slo);
                                    return queryProvider.getMetricQuery(configDTO, metric, slo, filteredResources,
                                        resourceIds, mainResourceIds, regionResources, platformResources, instanceTypeResources);
                                })
                                .reduce((currSet, nextSet) -> {
                                    currSet.retainAll(nextSet);
                                    return currSet;
                                })
                                .switchIfEmpty(Single.just(Set.of()));
                        }
                    });
            });
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
