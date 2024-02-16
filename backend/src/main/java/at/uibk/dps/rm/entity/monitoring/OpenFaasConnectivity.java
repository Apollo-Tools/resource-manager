package at.uibk.dps.rm.entity.monitoring;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Represents the region connectivity entity.
 *
 * @author matthi-g
 */
@Getter
@Setter
public class OpenFaasConnectivity implements Serializable {

    private Double latencySeconds;

    private Long resourceId;
}
