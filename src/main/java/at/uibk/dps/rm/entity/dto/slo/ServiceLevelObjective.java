package at.uibk.dps.rm.entity.dto.slo;

import at.uibk.dps.rm.util.ServiceLevelObjectiveDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(using = ServiceLevelObjectiveDeserializer.class)
public class ServiceLevelObjective {
    private String name;

    private ExpressionType expression;

    private List<SLOValue> value;

    public ServiceLevelObjective(String name, ExpressionType expression, List<SLOValue> value) {
        this.name = name;
        this.expression = expression;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ExpressionType getExpression() {
        return expression;
    }

    public void setExpression(ExpressionType expression) {
        this.expression = expression;
    }

    public List<SLOValue> getValue() {
        return value;
    }

    public void setValue(List<SLOValue> value) {
        this.value = value;
    }

}
