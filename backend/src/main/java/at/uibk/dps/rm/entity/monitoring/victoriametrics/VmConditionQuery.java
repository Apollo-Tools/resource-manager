package at.uibk.dps.rm.entity.monitoring.victoriametrics;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import at.uibk.dps.rm.entity.dto.slo.SLOValue;
import at.uibk.dps.rm.exception.BadInputException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public class VmConditionQuery implements VmQuery {

    private final VmQuery query;

    private final List<SLOValue> comparators;

    private final ExpressionType expressionType;

    @Override
    public String toString() {
        return "(" + query + ")" + expressionType.getSymbol() +
            comparators.stream()
                .map(sloValue -> {
                    switch (sloValue.getSloValueType()) {
                        case NUMBER:
                            return sloValue.getValueNumber().toString();
                        case BOOLEAN:
                            return sloValue.getValueBool() ? "1" : "0";
                        case STRING:
                        default:
                            throw new BadInputException("string values not supported");
                    }
                })
                .map(Object::toString).collect(Collectors.joining("%20or%20"));
    }
}
