package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.usertypes.StringArrayType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

/**
 * Represents the service entity.
 *
 * @author matthi-g
 */
@Entity
@Getter
@Setter
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long serviceId;

    private String name;

    private String image;

    private Integer replicas;

    @Convert(converter = StringArrayType.class)
    @Column(columnDefinition = "_text")
    private List<String> ports;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Column(precision = 3, scale = 3)
    private BigDecimal cpu;

    private Integer memory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id")
    private ServiceType serviceType;

    @Column(insertable = false, updatable = false)
    private  @Setter(AccessLevel.NONE) Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Service that = (Service) obj;
        return serviceId.equals(that.serviceId);
    }

    @Override
    @Generated
    public int hashCode() {
        return serviceId.hashCode();
    }
}
