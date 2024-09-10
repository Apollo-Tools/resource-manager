package at.uibk.dps.rm.entity.alerting;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the typ of alerting messages. Currently only SLO_VIOLATION is implemented.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum AlertType {

    SLO_VIOLATION("slo-violation");

    private final String value;
}
