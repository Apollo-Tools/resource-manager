package at.uibk.dps.rm.entity.dto.resource;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import at.uibk.dps.rm.entity.model.MainResource;
import at.uibk.dps.rm.entity.model.SubResource;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Set;

@Getter
@Setter
public class FindResourceBySloDTO {

    private final Long resourceId;

    private final String name;

    private final boolean isLockable;

    private final boolean isLocked;

    private final long regionId;

    private final long providerId;

    private final long environmentId;

    private final long platformId;

    private final long resourceTypeId;

    private final Set<MonitoredMetricValue> monitoredMetricValues;

    private final Timestamp createdAt;

    private final Timestamp updatedAt;

    public FindResourceBySloDTO(SubResourceDTO subResource) {
        this.resourceId = subResource.getResourceId();
        this.name = subResource.getName();
        this.isLockable = subResource.getIsLockable();
        this.isLocked = subResource.getIsLocked();
        this.regionId = subResource.getRegion().getRegionId();
        this.providerId = subResource.getRegion().getResourceProvider().getProviderId();
        this.environmentId = subResource.getRegion().getResourceProvider().getEnvironment().getEnvironmentId();
        this.platformId = subResource.getPlatform().getPlatformId();
        this.resourceTypeId = subResource.getPlatform().getResourceType().getTypeId();
        this.monitoredMetricValues = subResource.getMonitoredMetricValues();
        this.createdAt = subResource.getCreatedAt();
        this.updatedAt = subResource.getUpdatedAt();
    }

    public FindResourceBySloDTO(SubResource subResource) {
        this(subResource.getMainResource());
    }


    public FindResourceBySloDTO(MainResource mainResource) {
        this.resourceId = mainResource.getResourceId();
        this.name = mainResource.getName();
        this.isLockable = mainResource.getIsLockable();
        this.isLocked = mainResource.getIsLocked();
        this.regionId = mainResource.getRegion().getRegionId();
        this.providerId = mainResource.getRegion().getResourceProvider().getProviderId();
        this.environmentId = mainResource.getRegion().getResourceProvider().getEnvironment().getEnvironmentId();
        this.platformId = mainResource.getPlatform().getPlatformId();
        this.resourceTypeId = mainResource.getPlatform().getResourceType().getTypeId();
        this.monitoredMetricValues = mainResource.getMonitoredMetricValues();
        this.createdAt = mainResource.getCreatedAt();
        this.updatedAt = mainResource.getUpdatedAt();
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
