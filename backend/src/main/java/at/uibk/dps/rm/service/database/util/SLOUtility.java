package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import lombok.AllArgsConstructor;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletableFuture;
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
     * @param session the database session
     * @param sloRequest the slo request
     * @return a CompletableFuture that emits a list of the filtered and sorted resources
     */
    public CompletableFuture<List<Resource>> findAndFilterResourcesBySLOs(Session session, SLORequest sloRequest) {
        List<CompletableFuture<Void>> checkSLOs = sloRequest.getServiceLevelObjectives().stream().map(slo ->
                metricRepository.findByMetricAndIsSLO(session, slo.getName())
                    .thenAccept(metric -> validateSLOType(slo, metric))
                    .toCompletableFuture()
            )
            .collect(Collectors.toList());
        return CompletableFuture.allOf(checkSLOs.toArray(CompletableFuture[]::new))
            .thenCompose(result -> {
                List<String> sloNames = sloRequest.getServiceLevelObjectives().stream()
                    .map(ServiceLevelObjective::getName)
                    .collect(Collectors.toList());
                return resourceRepository.findAllBySLOs(session, sloNames, sloRequest.getEnvironments(),
                        sloRequest.getResourceTypes(), sloRequest.getPlatforms(), sloRequest.getRegions(),
                        sloRequest.getProviders())
                    .toCompletableFuture();
            })
            .thenApply(resources -> SLOCompareUtility.filterAndSortResourcesBySLOs(resources,
                sloRequest.getServiceLevelObjectives()));
    }

    /**
     * Validate whether the data type of the slo is the same as the data type of the metric.
     *
     * @param slo the service level objective
     * @param metric the metric
     */
    private void validateSLOType(ServiceLevelObjective slo, Metric metric) {
        ServiceResultValidator.checkFound(metric, ServiceLevelObjective.class);
        String sloValueType = slo.getValue().get(0).getSloValueType().name();
        String metricValueType = metric.getMetricType().getType().toUpperCase();
        boolean checkForTypeMatch = sloValueType.equals(metricValueType);
        if (!checkForTypeMatch) {
            throw new BadInputException("bad input type for service level objective " + slo.getName());
        }
    }
}
