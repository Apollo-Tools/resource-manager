package at.uibk.dps.rm.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Represents the metric_value entity.
 *
 * @author matthi-g
 */
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

    /**
     * Implementation from : <a href="https://jpa-buddy.com/blog/hopefully-the-final-article-about-equals-and-hashcode-for-jpa-entities-with-db-generated-ids/">...</a>
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        Class<?> oEffectiveClass = obj instanceof HibernateProxy ? ((HibernateProxy) obj).getHibernateLazyInitializer().getPersistentClass() : obj.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        MetricValue metricValue = (MetricValue) obj;
        return getMetricValueId() != null && Objects.equals(getMetricValueId(), metricValue.getMetricValueId());
    }

    /**
     * Implementation from : <a href="https://jpa-buddy.com/blog/hopefully-the-final-article-about-equals-and-hashcode-for-jpa-entities-with-db-generated-ids/">...</a>
     */
    @Override
    public int hashCode() {
        return this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode()
                : getClass().hashCode();
    }

    /**
     * Set the number value.
     *
     * @param value the new value
     */
    @JsonIgnore
    public void setValue(Double value) {
        setValueNumber(value);
    }

    /**
     * Set the string value.
     *
     * @param value the new value
     */
    @JsonIgnore
    public void setValue(String value) {
        setValueString(value);
    }

    /**
     * Set the boolean value
     *
     * @param value the new value
     */
    @JsonIgnore
    public void setValue(Boolean value) {
        setValueBool(value);
    }
}
