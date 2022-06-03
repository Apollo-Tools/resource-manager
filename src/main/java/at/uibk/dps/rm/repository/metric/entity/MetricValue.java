package at.uibk.dps.rm.repository.metric.entity;

import at.uibk.dps.rm.repository.resource.entity.Resource;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class MetricValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer metricValueId;

    private Long count;

    @Column(precision = 20, scale = 10)
    @Type(type = "big_decimal")
    private Double value;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id")
    private Metric metric;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    public Integer getMetricValueId() {
        return metricValueId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Metric getMetric() {
        return metric;
    }

    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        MetricValue that = (MetricValue) o;

        return metricValueId.equals(that.metricValueId);
    }

    @Override public int hashCode() {
        return metricValueId.hashCode();
    }
}
