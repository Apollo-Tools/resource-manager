package at.uibk.dps.rm.entity.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represents the artifact type entity with type function.
 *
 * @author matthi-g
 */
@Entity
@DiscriminatorValue("function")
@Getter
@Setter
public class FunctionType extends ArtifactType {
}
