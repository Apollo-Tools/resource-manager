package at.uibk.dps.rm.entity.dto.metric;

import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * Represents the value of a monitored metric.
 *
 * @author matthi-g
 */
@Getter
public class MonitoredMetricValue {

    private final String metric;

    @Getter
    private Double valueNumber;

    private String valueString;

    private Boolean valueBool;

    @Getter(AccessLevel.NONE)
    @JsonIgnore
    private Boolean fulfillsSLO;

    /**
     * Create in instance from the metricEnum.
     *
     * @param metricEnum the monitored metric
     */
    public MonitoredMetricValue(MonitoringMetricEnum metricEnum) {
        metric = metricEnum.getName();
    }

    /**
     * Set the value as a number and return the current instance.
     *
     * @param valueNumber the new value
     * @return the current instance
     */
    public MonitoredMetricValue setValueNumber(Double valueNumber) {
        this.valueNumber = valueNumber;
        return this;
    }

    /**
     * Set the value as a string and return the current instance.
     *
     * @param valueString the new value
     * @return the current instance
     */
    public MonitoredMetricValue setValueString(String valueString) {
        this.valueString = valueString;
        return this;
    }

    /**
     * Set the value as a boolean and return the current instance.
     *
     * @param valueBool the new value
     * @return the current instance
     */
    public MonitoredMetricValue setValueBool(Boolean valueBool) {
        this.valueBool = valueBool;
        return this;
    }

    /**
     * Set whether the current instance fulfills its service level objective.
     *
     * @param fulfillsSLO whether the service level objective is fulfilled
     * @return the current instance
     */
    public MonitoredMetricValue setFulfillsSLO(Boolean fulfillsSLO) {
        this.fulfillsSLO = fulfillsSLO;
        return this;
    }

    @JsonIgnore
    public boolean getFulfillsSLO(){
        return this.fulfillsSLO;
    }
}
