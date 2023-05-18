package at.uibk.dps.rm.entity.model.usertypes;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to define database arrays. Currently only works with items of type long.
 * For other types use {@link ArrayConverter}.
 *
 * @param <T> The type of the array items
 * @author matthi-g
 */
@SuppressWarnings("unchecked")
public abstract class ArrayType<T> implements UserType {
    @Override
    public int[] sqlTypes() {
        return new int[]{Types.ARRAY};
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Class<List> returnedClass() {
        return List.class;
    }

    @Override
    public Object nullSafeGet(ResultSet resultSet, String[] names, SharedSessionContractImplementor session,
            Object owner) throws HibernateException, SQLException {
        Object array = resultSet.getObject(names[0]);
        return array == null ? null : List.of((T[]) array);
    }

    @Override
    public void nullSafeSet(PreparedStatement preparedStatement, Object value, int index,
            SharedSessionContractImplementor session) throws HibernateException, SQLException {
        if (value != null && preparedStatement != null) {
            List<T> valueList = (List<T>) value;
            preparedStatement.setObject(index, valueList.toArray());
        } else {
            if (preparedStatement != null) {
                preparedStatement.setNull(index, sqlTypes()[0]);
            }
        }
    }

    @Override
    public boolean equals(Object obj1, Object obj2) throws HibernateException {
        return obj1 != null && obj1.equals(obj2);
    }

    @Override
    public int hashCode(Object object) throws HibernateException {
        return object == null ? 0 : object.hashCode();
    }

    @Override
    public Object deepCopy(Object value) throws HibernateException {
        if (!(value instanceof List)) {
            return null;
        }
        return new ArrayList<>((List<?>) value);
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Object value) throws HibernateException {
        return (Serializable) value;
    }

    @Override
    public Object assemble(Serializable cached, Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(Object original, Object target, Object owner) throws HibernateException {
        return original;
    }
}
