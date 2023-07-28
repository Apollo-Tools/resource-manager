package at.uibk.dps.rm.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subresource_id")
    private MainResource mainResource;
}
