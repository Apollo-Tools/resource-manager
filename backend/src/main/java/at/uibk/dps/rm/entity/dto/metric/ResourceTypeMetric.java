package at.uibk.dps.rm.entity.dto.metric;

import at.uibk.dps.rm.entity.model.Metric;
import lombok.Data;

@Data
public class ResourceTypeMetric {
    private Metric metric;
    private boolean required;
}
