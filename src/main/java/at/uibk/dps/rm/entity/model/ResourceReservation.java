package at.uibk.dps.rm.entity.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public class ResourceReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceReservationId;

    @JsonAlias("is_deployed")
    private Boolean isDeployed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id")
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @Column(insertable = false, updatable = false)
    private Timestamp createdAt;

    public Long getResourceReservationId() {
        return resourceReservationId;
    }

    public void setResourceReservationId(Long resourceReservationId) {
        this.resourceReservationId = resourceReservationId;
    }

    @JsonGetter("is_deployed")
    public Boolean getDeployed() {
        return isDeployed;
    }

    @JsonSetter("is_deployed")
    public void setDeployed(Boolean deployed) {
        isDeployed = deployed;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceReservation that = (ResourceReservation) o;

        return resourceReservationId.equals(that.resourceReservationId);
    }

    @Override
    public int hashCode() {
        return resourceReservationId.hashCode();
    }
}
