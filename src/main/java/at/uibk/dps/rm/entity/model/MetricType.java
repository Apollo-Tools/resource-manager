package at.uibk.dps.rm.entity.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MetricType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricTypeId;

    private String type;

    public Long getMetricTypeId() {
        return metricTypeId;
    }

    public void setMetricTypeId(Long metricTypeId) {
        this.metricTypeId = metricTypeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetricType that = (MetricType) o;

        return metricTypeId.equals(that.metricTypeId);
    }

    @Override
    public int hashCode() {
        return metricTypeId.hashCode();
    }
}
