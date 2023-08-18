package at.uibk.dps.rm.entity.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represents the artifact type entity with type service.
 *
 * @author matthi-g
 */
@Entity
@DiscriminatorValue("service")
@Getter
@Setter
public class ServiceType extends ArtifactType {
}
