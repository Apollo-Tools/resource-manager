package at.uibk.dps.rm.entity.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

/**
 * Represents the resource_reservation entity with type function.
 *
 * @author matthi-g
 */
@Entity
@DiscriminatorValue("function")
@Getter
@Setter
public class FunctionReservation extends ResourceReservation {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id")
    private Function function;
}
