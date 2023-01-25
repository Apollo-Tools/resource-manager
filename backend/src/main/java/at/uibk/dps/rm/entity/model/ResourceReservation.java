package at.uibk.dps.rm.entity.model;

import at.uibk.dps.rm.annotations.Generated;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class ResourceReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceReservationId;

    @JsonProperty("is_deployed")
    private Boolean isDeployed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_resource_id")
    private FunctionResource functionResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(insertable = false, updatable = false)
    private  @Setter(value = AccessLevel.NONE) Timestamp createdAt;

    @Override
    @Generated
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceReservation that = (ResourceReservation) o;

        return resourceReservationId.equals(that.resourceReservationId);
    }

    @Override
    @Generated
    public int hashCode() {
        return resourceReservationId.hashCode();
    }
}
