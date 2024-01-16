package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VmCompoundQuery {

    private final VmSingleQuery queryOne;

    private final VmSingleQuery queryTwo;

    private final String binaryOperator;

    private double multiplier = 1.0;

    private double summand = 0.0;

    public VmCompoundQuery setMultiplier(double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public VmCompoundQuery setSummand(double summand) {
        this.summand = summand;
        return this;
    }

    @Override
    public String toString() {
        return "(" + queryOne.toString() + binaryOperator + queryTwo.toString() + ")" +
            (multiplier == 1.0 ? "" : " * " + multiplier) +
            (summand == 0.0 ? "" : " + " + summand);
    }
}
