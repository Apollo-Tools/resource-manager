package at.uibk.dps.rm.entity.model.usertypes;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Convert an object of type {@link ExpressionType} into a string value and vice versa.
 *
 * @author matthi-g
 */
@Converter
public class ExpressionConverter implements AttributeConverter<ExpressionType, String> {
    @Override
    public String convertToDatabaseColumn(ExpressionType expression) {
        return expression == null ? null : expression.getSymbol();
    }

    @Override
    public ExpressionType convertToEntityAttribute(String symbol) {
        return symbol == null ? null : ExpressionType.fromString(symbol);
    }
}
