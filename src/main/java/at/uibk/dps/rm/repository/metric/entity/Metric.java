package at.uibk.dps.rm.repository.metric.entity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class Metric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricId;

    private String metric;

    private String description;

    @Column(insertable = false, updatable = false)
    private Timestamp createdAt;

    public Long getMetricId() {
        return metricId;
    }

    public void setMetricId(Long metricId) {
        this.metricId = metricId;
    }

    public String getMetric() {
        return metric;
    }

    public void setMetric(String metric) {
        this.metric = metric;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Metric metric = (Metric) o;

        return metricId.equals(metric.metricId);
    }

    @Override public int hashCode() {
        return metricId.hashCode();
    }
}
