package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
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
     * @param sessionManager the database session manager
     * @param sloRequest the slo request
     * @return a CompletableFuture that emits a list of the filtered and sorted resources
     */
    public Single<List<Resource>> findAndFilterResourcesBySLOs(SessionManager sessionManager,
                                                               SLORequest sloRequest) {
        Completable checkSLOs = Observable.fromIterable(sloRequest.getServiceLevelObjectives())
            .map(slo -> metricRepository.findByMetricAndIsSLO(sessionManager, slo.getName())
                .switchIfEmpty(Maybe.error(new NotFoundException(ServiceLevelObjective.class)))
                .flatMapCompletable(metric -> Completable.fromAction(() -> validateSLOType(slo, metric))))
            .toList()
            .flatMapCompletable(Completable::merge);
        List<String> sloNames = sloRequest.getServiceLevelObjectives().stream()
            .map(ServiceLevelObjective::getName)
            .collect(Collectors.toList());
        return checkSLOs
            .andThen(Single.defer(() -> resourceRepository.findAllBySLOs(sessionManager, sloNames,
                sloRequest.getEnvironments(), sloRequest.getResourceTypes(), sloRequest.getPlatforms(),
                sloRequest.getRegions(), sloRequest.getProviders()))
            )
            .map(resources -> SLOCompareUtility.filterAndSortResourcesBySLOs(resources,
                sloRequest.getServiceLevelObjectives()));
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
