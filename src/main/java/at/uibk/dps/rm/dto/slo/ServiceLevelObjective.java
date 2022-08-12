package at.uibk.dps.rm.dto.slo;

import at.uibk.dps.rm.util.ServiceLevelObjectiveDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

@JsonDeserialize(using = ServiceLevelObjectiveDeserializer.class)
public class ServiceLevelObjective {
    private String name;

    private ExpressionType expression;

    private List<SLOValue> value;

    private PropertyType propertyType;

    public ServiceLevelObjective(String name, ExpressionType expression, List<SLOValue> value, PropertyType propertyType) {
        this.name = name;
        this.expression = expression;
        this.value = value;
        this.propertyType = propertyType;
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

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }
}
