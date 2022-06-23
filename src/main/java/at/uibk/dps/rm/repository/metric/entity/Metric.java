package at.uibk.dps.rm.repository.metric.entity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class Metric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metric_id;

    private String metric;

    private String description;

    @Column(insertable = false, updatable = false)
    private Timestamp createdAt;

    public Long getMetric_id() {
        return metric_id;
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

        return metric_id.equals(metric.metric_id);
    }

    @Override public int hashCode() {
        return metric_id.hashCode();
    }
}
