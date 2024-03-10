package at.uibk.dps.rm.entity.alerting;

import at.uibk.dps.rm.entity.dto.metric.MonitoredMetricValue;
import lombok.Data;

/**
 * Represents a message that is sent to the client. Currently only alerts of type SLO-Breach
 * are implemented. These messages are sent, if the alerting handler observes and SLO-Breach for
 * an active deployment.
 *
 * @author matthi-g
 */
@Data
public class AlertMessage {

    private final AlertType type;

    private final long resourceId;

    private final String metric;

    private final Object value;

    private final long timestamp;

    /**
     * Create a new instance from the type, resourceId and metricValue.
     *
     * @param type the type of the alert
     * @param resourceId the id of the affected resource
     * @param metricValue the observed metric value that is responsible for the SLO-Breach
     */
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
