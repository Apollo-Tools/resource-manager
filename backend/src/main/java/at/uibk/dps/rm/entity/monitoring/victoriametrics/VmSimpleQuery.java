package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import lombok.RequiredArgsConstructor;

/**
 * Implements a simple <a href=https://victoriametrics.com/>VictoriaMetrics</a> query to add
 * a multiplier and summand to a {@link VmQuery}.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
public class VmSimpleQuery implements VmQuery {

    private final VmQuery subQuery;

    private double multiplier = 1.0;

    private double summand = 0.0;

    /**
     * Set the multiplier of the query and return the current instance.
     *
     * @param multiplier the multiplier
     * @return the current instance
     */
    public VmSimpleQuery setMultiplier(double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    /**
     * Set the summand of the query and return the current instance.
     *
     * @param summand the summand
     * @return the current instance
     */
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
