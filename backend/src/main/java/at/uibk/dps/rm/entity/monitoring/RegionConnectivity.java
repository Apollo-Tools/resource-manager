package at.uibk.dps.rm.entity.monitoring;

import at.uibk.dps.rm.entity.model.Region;
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
public class RegionConnectivity implements Serializable {

    private Double latencySeconds;

    private Boolean isOnline;

    private Region region;
}
