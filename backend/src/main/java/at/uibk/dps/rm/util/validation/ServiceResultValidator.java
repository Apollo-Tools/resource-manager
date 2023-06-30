package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import lombok.experimental.UtilityClass;

/**
 * This class is used for Validation of the results from database operations that are part
 * of the service classes.
 *
 * @author matthi-g
 */
@UtilityClass
public class ServiceResultValidator {

    public static void checkFound(Object object, Class<?> entityClass) {
        if (object == null) {
            throw new NotFoundException(entityClass);
        }
    }

    public static void checkExists(Object object, Class<?> entityClass) {
        if (object != null) {
            throw new AlreadyExistsException(entityClass);
        }
    }
}
