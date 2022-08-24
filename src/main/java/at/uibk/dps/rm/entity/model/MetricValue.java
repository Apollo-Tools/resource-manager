package at.uibk.dps.rm.entity.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
public class MetricValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricValueId;

    @Column(nullable = false)
    private Long count = 0L;

    @Column(precision = 20, scale = 10)
    @Type(type = "big_decimal")
    private BigDecimal valueNumber;

    private String valueString;

    private Boolean valueBool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id")
    private Metric metric;

    @Column(insertable = false, updatable = false)
    private Timestamp createdAt;

    @Column(insertable = false, updatable = false)
    private Timestamp updatedAt;

    public Long getMetricValueId() {
        return metricValueId;
    }

    public void setMetricValueId(Long metricValueId) {
        this.metricValueId = metricValueId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public BigDecimal getValueNumber() {
        return valueNumber;
    }

    public void setValueNumber(double value) {
        this.valueNumber = BigDecimal.valueOf(value);
    }

    public void setValueNumber(BigDecimal valueNumber) {
        this.valueNumber = valueNumber;
    }

    public String getValueString() {
        return valueString;
    }

    public void setValueString(String valueString) {
        this.valueString = valueString;
    }

    public Boolean getValueBool() {
        return valueBool;
    }

    public void setValueBool(Boolean valueBool) {
        this.valueBool = valueBool;
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
