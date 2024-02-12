package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import lombok.RequiredArgsConstructor;

/**
 * Implements a compound <a href=https://victoriametrics.com/>VictoriaMetrics</a> query that
 * consists of two {@link VmQuery}s.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class VmCompoundQuery implements VmQuery {

    private final VmQuery queryOne;

    private final VmQuery queryTwo;

    private final char binaryOperator;

    private double multiplier = 1.0;

    private double summand = 0.0;

    /**
     * Set the multiplier of the query and return the current instance.
     *
     * @param multiplier the multiplier
     * @return the current instance
     */
    public VmCompoundQuery setMultiplier(double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    /**
     * Set the summand of the query and return the current instance.
     *
     * @param summand the summand
     * @return the current instance
     */
    public VmCompoundQuery setSummand(double summand) {
        this.summand = summand;
        return this;
    }

    @Override
    public String toString() {
        return "(" + queryOne.toString() + binaryOperator + queryTwo.toString() + ")" +
            (multiplier == 1.0 ? "" : "*" + multiplier) +
            (summand == 0.0 ? "" : "+" + summand);
    }
}
