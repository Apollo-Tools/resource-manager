package at.uibk.dps.rm.entity.dto.deployment;

import lombok.Data;

/**
 * Represents function resource ids that are part of deploy resources request.
 *
 * @author matthi-g
 */
@Data
public class FunctionResourceIds {
    private long functionId;

    private long resourceId;
}
