package at.uibk.dps.rm.entity.dto.ensemble;

import lombok.Data;

/**
 * Represents one entry in the validateEnsemble response
 *
 * @author matthi-g
 */
@Data
public class ResourceEnsembleStatus {
    private final long resourceId;

    private final boolean isValid;
}
