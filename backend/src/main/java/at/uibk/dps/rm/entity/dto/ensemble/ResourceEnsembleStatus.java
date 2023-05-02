package at.uibk.dps.rm.entity.dto.ensemble;

import lombok.Data;

@Data
public class ResourceEnsembleStatus {
    private final long resourceId;

    private final boolean isValid;
}
