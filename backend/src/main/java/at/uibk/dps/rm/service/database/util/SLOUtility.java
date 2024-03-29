package at.uibk.dps.rm.service.database.util;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import io.reactivex.rxjava3.core.Single;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * This class provides methods to validate SLOs and find and filter resources by SLOs.
 *
 * @author matthi-g
 */
@AllArgsConstructor
public class SLOUtility {

    private final ResourceRepository resourceRepository;

    /**
     * Find resources by non monitored service level objectives from the sloRequest.
     *
     * @param sm the database session manager
     * @param sloRequest the slo request
     * @return a Single that emits a list of the filtered resources
     */
    public Single<List<Resource>> findResourcesByNonMonitoredSLOs(SessionManager sm,
            SLORequest sloRequest) {
        if (sloRequest.getEnvironments().isEmpty() && sloRequest.getResourceTypes().isEmpty()
            && sloRequest.getPlatforms().isEmpty() && sloRequest.getRegions().isEmpty() &&
            sloRequest.getProviders().isEmpty()) {
            return resourceRepository.findAllMainAndSubResourcesAndFetch(sm);
        }
        return resourceRepository.findAllByNonMVSLOs(sm, sloRequest.getEnvironments(),
            sloRequest.getResourceTypes(), sloRequest.getPlatforms(), sloRequest.getRegions(),
            sloRequest.getProviders());
    }

    /**
     * Validate whether the data type of the slo is the same as the data type of the metric.
     *
     * @param slo the service level objective
     * @param metric the metric
     */
    public static void validateSLOType(ServiceLevelObjective slo, Metric metric) {
        String sloValueType = slo.getValue().get(0).getSloValueType().name();
        String metricValueType = metric.getMetricType().getType().toUpperCase();
        boolean checkForTypeMatch = sloValueType.equals(metricValueType);
        if (!checkForTypeMatch) {
            throw new BadInputException("bad input type for service level objective " + slo.getName());
        }
    }
}
