package at.uibk.dps.rm.entity.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Represents the service_deployment entity.
 *
 * @author matthi-g
 */
@Entity
@DiscriminatorValue("service")
@Getter
@Setter
public class ServiceDeployment extends ResourceDeployment {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "service_id")
    private Service service;

    private String namespace;

    private String context;
}
