package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class MetricValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long metricValueId;

    private Long count = 0L;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Column(precision = 20, scale = 10)
    @Type(type = "big_decimal")
    private BigDecimal valueNumber;

    private String valueString;

    private Boolean valueBool;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id")
    private Metric metric;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Column(insertable = false, updatable = false)
    private @Setter(AccessLevel.NONE) Timestamp updatedAt;

    public void setValueNumber(final Double value) {
        if (value == null) {
            this.valueNumber = null;
        } else {
            this.valueNumber = BigDecimal.valueOf(value);
        }
    }

    @Override
    @Generated
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final MetricValue that = (MetricValue) obj;
        return metricValueId.equals(that.metricValueId);
    }

    @Override
    @Generated
    public int hashCode() {
        return metricValueId.hashCode();
    }
}
