package at.uibk.dps.rm.entity.monitoring;

import lombok.Getter;

import java.util.Map;

/**
 * Represents an object that consists of a metric, value and tags that is compatible with
 * <a href=http://opentsdb.net/>OpenTSDB</a>}.
 *
 * @author matthi-g
 */
@Getter
public class OpenTSDBEntity {

    /**
     * Create a new instance from a metric, long value and tags.
     *
     * @param metric the metric
     * @param value the value
     * @param tags the tags
     */
    public OpenTSDBEntity(String metric, long value, Map<String, String> tags) {
        this.metric = metric;
        this.value = value;
        this.tags = tags;
    }

    /**
     * Create a new instance from a metric, double value and tags.
     *
     * @param metric the metric
     * @param value the value
     * @param tags the tags
     */
    public OpenTSDBEntity(String metric, double value, Map<String, String> tags) {
        this.metric = metric;
        this.value = value;
        this.tags = tags;
    }

    private final String metric;

    private final Object value;

    private final Map<String, String> tags;
}
