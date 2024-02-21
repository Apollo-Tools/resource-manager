package at.uibk.dps.rm.entity.alerting;

import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import lombok.Data;

@Data
public class AlertMessage {

    private final AlertType type;

    private final long resourceId;

    private final String metric;

    private final Object value;

    private final long timestamp;

    public AlertMessage(AlertType type, long resourceId, MonitoredMetricValue metricValue) {
        this.type = type;
        this.resourceId = resourceId;
        this.metric = metricValue.getMetric();
        if (metricValue.getValueNumber() != null) {
            this.value = metricValue.getValueNumber();
        } else if (metricValue.getValueString() != null) {
            this.value = metricValue.getValueString();
        } else if (metricValue.getValueBool() != null) {
            this.value = metricValue.getValueBool();
        } else {
            this.value = null;
        }
        timestamp = System.currentTimeMillis() / 1000L;
    }

}
