package at.uibk.dps.rm.entity.alerting;


import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AlertType {

    SLO_BREACH("slo-breach");

    private final String value;
}
