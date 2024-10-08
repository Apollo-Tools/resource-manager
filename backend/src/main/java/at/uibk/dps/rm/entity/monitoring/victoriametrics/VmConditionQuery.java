package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.exception.BadInputException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements a condition <a href=https://victoriametrics.com/>VictoriaMetrics</a> query that
 * adds a codition to a {@link VmQuery}.
 *
 * @author matthi-g
 */
@RequiredArgsConstructor
@Getter
public class VmConditionQuery implements VmQuery {

    private final VmQuery query;

    private final List<SLOValue> comparators;

    private final ExpressionType expressionType;

    @Override
    public String toString() {
        return comparators.stream()
            .map(sloValue -> {
                String value;
                switch (sloValue.getSloValueType()) {
                    case NUMBER:
                        value = sloValue.getValueNumber().toString();
                        break;
                    case BOOLEAN:
                        value = sloValue.getValueBool() ? "1" : "0";
                        break;
                    case STRING:
                    default:
                        throw new BadInputException("value type is not supported");
                }
                return "((" + query + ")" + expressionType.getSymbol() + value + ")";
            }).map(Object::toString).collect(Collectors.joining("%20or%20"));
    }
}
