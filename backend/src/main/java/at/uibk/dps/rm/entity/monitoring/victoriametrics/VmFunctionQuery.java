package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class VmFunctionQuery implements VmQuery {

    private final String function;

    private final VmQuery subQuery;

    private Set<String> parameters = new HashSet<>();

    private Set<String> groupBy = new HashSet<>();

    private double multiplier = 1.0;

    private double summand = 0.0;

    public VmFunctionQuery setParameters(Set<String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public VmFunctionQuery setGroupBy(Set<String> groupBy) {
        this.groupBy = groupBy;
        return this;
    }

    public VmFunctionQuery setMultiplier(Double multiplier) {
        this.multiplier = multiplier;
        return this;
    }

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
