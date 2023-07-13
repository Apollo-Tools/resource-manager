package at.uibk.dps.rm.entity.dto.metric;

import at.uibk.dps.rm.entity.model.Metric;
import lombok.Data;

/**
 * Represents metrics that are used for the response
 *
 * @author matthi-g
 */
@Data
public class PlatformMetric {
    private Metric metric;
    private boolean required;
}
