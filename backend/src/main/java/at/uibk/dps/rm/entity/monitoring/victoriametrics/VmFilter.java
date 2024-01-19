package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
@Getter
public class VmFilter {

    private final String label;

    private final String operator;

    private final Set<String> values;

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        VmFilter vmFilter = (VmFilter) object;
        return label.equals(vmFilter.label);
    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }
}
