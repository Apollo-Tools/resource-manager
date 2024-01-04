package at.uibk.dps.rm.entity.monitoring.opentsdb;

import lombok.Getter;

import java.util.Map;

@Getter
public class OpenTSDBEntity {

    public OpenTSDBEntity(String metric, long value, Map<String, String> tags) {
        this.metric = metric;
        this.value = value;
        this.tags = tags;
    }

    public OpenTSDBEntity(String metric, double value, Map<String, String> tags) {
        this.metric = metric;
        this.value = value;
        this.tags = tags;
    }

    private final String metric;

    private final Object value;

    private final Map<String, String> tags;


}
