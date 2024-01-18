package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class VmSimpleQuery implements VmQuery {

    private final VmQuery subQuery;

    private double multiplier = 1.0;

    private double summand = 0.0;

    public VmSimpleQuery setMultiplier(double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    public VmSimpleQuery setSummand(double summand) {
        this.summand = summand;
        return this;
    }

    @Override
    public String toString() {
        return "(" + subQuery + ")" +
            (multiplier == 1.0 ? "" : "*" + multiplier) +
            (summand == 0.0 ? "" : "+" + summand);
    }
}
