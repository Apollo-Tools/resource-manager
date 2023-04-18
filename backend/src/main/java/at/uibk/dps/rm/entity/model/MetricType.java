package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class MetricType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricTypeId;

    private String type;

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final MetricType that = (MetricType) obj;
        return metricTypeId.equals(that.metricTypeId);
    }

    @Override
    @Generated
    public int hashCode() {
        return metricTypeId.hashCode();
    }
}
