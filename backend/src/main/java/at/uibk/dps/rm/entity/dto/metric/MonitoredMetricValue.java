package at.uibk.dps.rm.entity.dto.metric;

import at.uibk.dps.rm.entity.monitoring.MonitoringMetricEnum;
import lombok.Getter;

@Getter
public class MonitoredMetricValue {

    private final String metric;

    private Double valueNumber;

    private String valueString;

    private Boolean valueBool;

    public MonitoredMetricValue(MonitoringMetricEnum metricEnum) {
        metric = metricEnum.getName();
    }

    public MonitoredMetricValue setValueNumber(Double valueNumber) {
        this.valueNumber = valueNumber;
        return this;
    }

    public MonitoredMetricValue setValueString(String valueString) {
        this.valueString = valueString;
        return this;
    }

    public MonitoredMetricValue setValueBool(Boolean valueBool) {
        this.valueBool = valueBool;
        return this;
    }
}
