package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class provides methods to validate SLOs and find and filter resources by SLOs.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public class SLOUtility {
    private final ResourceRepository resourceRepository;

    private final MetricRepository metricRepository;

    /**
     * Find, filter and sort resources by service level objectives from the sloRequest.
     *
     * @param sm the database session manager
     * @param sloRequest the slo request
     * @return a CompletableFuture that emits a list of the filtered and sorted resources
     */
    public Single<List<Resource>> findAndFilterResourcesBySLOs(SessionManager sm,
            SLORequest sloRequest) {
        Single<List<String>> checkSLOs = metricRepository
            .findAllNonMonitoredBySLO(sm, sloRequest.getServiceLevelObjectives())
            .flatMapObservable(metrics -> Observable.fromIterable(sloRequest.getServiceLevelObjectives())
                .flatMap(slo -> Observable.fromIterable(metrics)
                    .filter(metric -> metric.getMetric().equals(slo.getName()))
                    .map(metric -> {
                        validateSLOType(slo, metric);
                        return metric;
                })))
            .map(Metric::getMetric)
            .toList();
        return checkSLOs.flatMap(metrics -> resourceRepository.findAllBySLOs(sm, metrics,
                sloRequest.getEnvironments(), sloRequest.getResourceTypes(), sloRequest.getPlatforms(),
                sloRequest.getRegions(), sloRequest.getProviders()))
            .flatMap(resources -> Observable.fromIterable(resources)
                .map(resource -> resource.getResourceId().toString())
                .collect(Collectors.joining("|"))
                .flatMap(resourcesString -> {
                    System.out.println(resourcesString);
                    return Single.just(resourcesString);
                })
                .map(result -> SLOCompareUtility.filterAndSortResourcesBySLOs(resources,
                    sloRequest.getServiceLevelObjectives())));
    }

    public Single<List<Resource>> findResourcesByNonMonitoredSLOs(SessionManager sm,
            SLORequest sloRequest) {
        Single<List<String>> checkSLOs = metricRepository
            .findAllNonMonitoredBySLO(sm, sloRequest.getServiceLevelObjectives())
            .flatMapObservable(metrics -> Observable.fromIterable(sloRequest.getServiceLevelObjectives())
                .flatMap(slo -> Observable.fromIterable(metrics)
                    .filter(metric -> metric.getMetric().equals(slo.getName()))
                    .map(metric -> {
                        validateSLOType(slo, metric);
                        return metric;
                })))
            .map(Metric::getMetric)
            .toList();
        return checkSLOs.flatMap(metrics -> {
            if (sloRequest.getEnvironments().isEmpty() && sloRequest.getResourceTypes().isEmpty()
                && sloRequest.getPlatforms().isEmpty() && sloRequest.getRegions().isEmpty() &&
                sloRequest.getProviders().isEmpty()) {
                return resourceRepository.findAllMainAndSubResourcesAndFetch(sm);
            }
            return resourceRepository.findAllByNonMVSLOs(sm, sloRequest.getEnvironments(),
                sloRequest.getResourceTypes(), sloRequest.getPlatforms(), sloRequest.getRegions(),
                sloRequest.getProviders());
        });
    }

    /**
     * Validate whether the data type of the slo is the same as the data type of the metric.
     *
     * @param slo the service level objective
     * @param metric the metric
     */
    private void validateSLOType(ServiceLevelObjective slo, Metric metric) {
        String sloValueType = slo.getValue().get(0).getSloValueType().name();
        String metricValueType = metric.getMetricType().getType().toUpperCase();
        boolean checkForTypeMatch = sloValueType.equals(metricValueType);
        if (!checkForTypeMatch) {
            throw new BadInputException("bad input type for service level objective " + slo.getName());
        }
    }
}
