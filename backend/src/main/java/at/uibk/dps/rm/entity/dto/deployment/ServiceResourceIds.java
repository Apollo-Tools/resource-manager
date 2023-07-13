package at.uibk.dps.rm.entity.dto.deployment;

import lombok.Data;

/**
 * Represents service resource ids that are part of deploy resources request.
 *
 * @author matthi-g
 */
@Data
public class ServiceResourceIds {
    private long serviceId;

    private long resourceId;
}
