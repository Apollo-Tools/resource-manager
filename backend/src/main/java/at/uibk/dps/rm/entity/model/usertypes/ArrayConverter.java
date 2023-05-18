package at.uibk.dps.rm.entity.model.usertypes;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.List;

/**
 * Used to convert sql arrays into a List and vice versa.
 *
 * @param <T> the type of the array items
 * @author matthi-g
 */
@Converter
public abstract class ArrayConverter<T> implements AttributeConverter<List<T>, Object> {
    @Override
    public Object convertToDatabaseColumn(List<T> attribute) {
        return attribute == null ? null : attribute.toArray();
    }

    @Override
    public List<T> convertToEntityAttribute(Object number) {
        //noinspection unchecked
        return number == null ? null : List.of((T[]) number);
    }
}
