package at.uibk.dps.rm.entity.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Represents the service_reservation entity.
 *
 * @author matthi-g
 */
@Entity
@DiscriminatorValue("service")
@Getter
@Setter
public class ServiceReservation extends ResourceReservation {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id")
    private Service service;

    private String namespace;

    private String context;
}
