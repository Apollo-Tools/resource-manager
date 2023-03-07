package at.uibk.dps.rm.entity.dto;

import at.uibk.dps.rm.entity.dto.metric.ResourceTypeMetric;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetResourceTypeMetricsResponse {
    List<ResourceTypeMetric> resourceTypeMetrics = new ArrayList<>();
}
