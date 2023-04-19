package at.uibk.dps.rm.entity.dto.metric;

import at.uibk.dps.rm.entity.model.Metric;
import lombok.Data;

/**
 * Represents metrics that are used for the respon
 *
 * @author matthi-g
 */
@Data
public class ResourceTypeMetric {
    private Metric metric;
    private boolean required;
}
