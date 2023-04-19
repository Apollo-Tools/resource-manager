package at.uibk.dps.rm.entity.dto.reservation;

import lombok.Data;

/**
 * Represents function resource ids that are part of reserve resource request.
 *
 * @author matthi-g
 */
@Data
public class FunctionResourceIds {
    private long functionId;

    private long resourceId;
}
