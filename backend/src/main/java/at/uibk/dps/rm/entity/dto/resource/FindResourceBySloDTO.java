package at.uibk.dps.rm.entity.dto.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.MainResource;
import at.uibk.dps.rm.util.validation.SLOCompareUtility;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.List;

/**
 * Represents a single resource entry of the listResourcesBySLOs response.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class FindResourceBySloDTO {

    private final Long resourceId;

    private final String name;

    private final boolean isLockable;

    private final boolean isLocked;

    private final boolean isMainResource;

    private final long regionId;

    private final long providerId;

    private final long environmentId;

    private final long platformId;

    private final long resourceTypeId;

    private final List<MonitoredMetricValue> monitoredMetricValues;

    private final Timestamp createdAt;

    private final Timestamp updatedAt;

    /**
     * Create a new instance from a subResource and sort the metric values by the
     * serviceLevelObjectives.
     *
     * @param subResource the subresource
     * @param serviceLevelObjectives the service level objectives
     */
    public FindResourceBySloDTO(SubResourceDTO subResource, List<ServiceLevelObjective> serviceLevelObjectives) {
        this.resourceId = subResource.getResourceId();
        this.name = subResource.getName();
        this.isLockable = subResource.getIsLockable();
        this.isLocked = subResource.getIsLocked();
        this.isMainResource = false;
        this.regionId = subResource.getRegion().getRegionId();
        this.providerId = subResource.getRegion().getResourceProvider().getProviderId();
        this.environmentId = subResource.getRegion().getResourceProvider().getEnvironment().getEnvironmentId();
        this.platformId = subResource.getPlatform().getPlatformId();
        this.resourceTypeId = subResource.getPlatform().getResourceType().getTypeId();
        this.createdAt = subResource.getCreatedAt();
        this.updatedAt = subResource.getUpdatedAt();
        this.monitoredMetricValues = SLOCompareUtility.sortMonitoredMetricValuesBySLOs(
            subResource.getMonitoredMetricValues(), serviceLevelObjectives);
    }

    /**
     * Create a new instance from a mainResource and sort the metric values by the
     * serviceLevelObjectives.
     *
     * @param mainResource the subresource
     * @param serviceLevelObjectives the service level objectives
     */
    public FindResourceBySloDTO(MainResource mainResource, List<ServiceLevelObjective> serviceLevelObjectives) {
        this.resourceId = mainResource.getResourceId();
        this.name = mainResource.getName();
        this.isLockable = mainResource.getIsLockable();
        this.isLocked = mainResource.getIsLocked();
        this.isMainResource = true;
        this.regionId = mainResource.getRegion().getRegionId();
        this.providerId = mainResource.getRegion().getResourceProvider().getProviderId();
        this.environmentId = mainResource.getRegion().getResourceProvider().getEnvironment().getEnvironmentId();
        this.platformId = mainResource.getPlatform().getPlatformId();
        this.resourceTypeId = mainResource.getPlatform().getResourceType().getTypeId();
        this.createdAt = mainResource.getCreatedAt();
        this.updatedAt = mainResource.getUpdatedAt();
        this.monitoredMetricValues = SLOCompareUtility.sortMonitoredMetricValuesBySLOs(
            mainResource.getMonitoredMetricValues(), serviceLevelObjectives);
    }


    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FindResourceBySloDTO resource = (FindResourceBySloDTO) obj;
        return resourceId.equals(resource.resourceId);
    }

    @Override
    @Generated
    public int hashCode() {
        return resourceId.hashCode();
    }
}
