package at.uibk.dps.rm.entity.alerting;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents the typ of alerting messages. Currently only SLO-Breach is implemented.
 *
 * @author matthi-g
 */
@AllArgsConstructor
@Getter
public enum AlertType {

    SLO_BREACH("slo-breach");

    private final String value;
}
