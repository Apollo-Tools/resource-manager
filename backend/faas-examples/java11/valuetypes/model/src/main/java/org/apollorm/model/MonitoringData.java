package org.apollorm.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the monitoring data of a function execution.
 *
 * @author matthi-g
 */
public class MonitoringData {

    @JsonProperty("execution_time_ms")
    private double executionTimeMs;

    @JsonProperty("start_timestamp")
    private long startTimestamp;

    public double getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(double executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }
}
