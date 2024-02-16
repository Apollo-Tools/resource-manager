package org.apollorm.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a response consisting of body which contains the result of the function execution
 * and the monitoring data.
 *
 * @author matthi-g
 */
public class ResponseWithMonitoring {

    @JsonProperty("monitoring_data")
    private MonitoringData monitoringData;

    private Object body;

    public MonitoringData getMonitoringData() {
        return monitoringData;
    }

    public void setMonitoringData(MonitoringData monitoringData) {
        this.monitoringData = monitoringData;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }
}
