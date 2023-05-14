package at.uibk.dps.rm.entity.dto.reservation;

import lombok.Data;

/**
 * Represents service resource ids that are part of reserve resource request.
 *
 * @author matthi-g
 */
@Data
public class ServiceResourceIds {
    private long serviceId;

    private long resourceId;

    private String namespace;

    private String context;
}
