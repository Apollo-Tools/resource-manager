package at.uibk.dps.rm.entity.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Represents the resource entity with type sub.
 *
 * @author matthi-g
 */
@Entity
@DiscriminatorValue("sub")
@Getter
@Setter
public class SubResource extends Resource {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "main_resource_id")
    private MainResource mainResource;
}
