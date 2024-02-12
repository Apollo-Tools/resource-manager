package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Implements a <a href=https://victoriametrics.com/>VictoriaMetrics</a> function query that
 * applies a function on a {@link VmQuery}.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
@Getter
public class VmFunctionQuery implements VmQuery {

    private final String function;

    private final VmQuery subQuery;

    private Set<String> parameters = new HashSet<>();

    private Set<String> groupBy = new HashSet<>();

    private double multiplier = 1.0;

    private double summand = 0.0;

    /**
     * Set the function parameters of the query and return the current instance.
     *
     * @param parameters the parameters
     * @return the current instance
     */
    public VmFunctionQuery setParameters(Set<String> parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * Set the groupBy labels of the query and return the current instance.
     *
     * @param groupBy the groupBy labels
     * @return the current instance
     */
    public VmFunctionQuery setGroupBy(Set<String> groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    /**
     * Set the multiplier of the query and return the current instance.
     *
     * @param multiplier the multiplier
     * @return the current instance
     */
    public VmFunctionQuery setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

    /**
     * Set the summand of the query and return the current instance.
     *
     * @param summand the summand
     * @return the current instance
     */
    public VmFunctionQuery setSummand(Double summand) {
        this.summand = summand;
        return this;
    }

    @Override
    public String toString() {
        return function + "(" + subQuery +
            (parameters.isEmpty() ? "" : "," + String.join(",", parameters)) + ")" +
            (groupBy.isEmpty() ? "" : "%20by%20(" + String.join(",", groupBy) + ")") +
            (multiplier == 1.0 ? "" : "*" + multiplier) +
            (summand == 0.0 ? "" : "+" + summand);
    }
}
