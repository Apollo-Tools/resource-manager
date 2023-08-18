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

    /**
     * Check if an object is null or not and throw {@link NotFoundException} if
     * it is null.
     *
     * @param object the object to check
     * @param entityClass the class of the object
     */
    public static void checkFound(Object object, Class<?> entityClass) {
        if (object == null) {
            throw new NotFoundException(entityClass);
        }
    }

    /**
     * Check if an object is null or not and throw {@link NotFoundException} with the message if
     * it is null.
     *
     * @param object the object to check
     * @param message the error message
     */
    public static void checkFound(Object object, String message) {
        if (object == null) {
            throw new NotFoundException(message);
        }
    }

    /**
     * Check if an object is null or not and throw {@link AlreadyExistsException} if
     * it is not null.
     *
     * @param object the object to check
     * @param entityClass the class of the object
     */
    public static void checkExists(Object object, Class<?> entityClass) {
        if (object != null) {
            throw new AlreadyExistsException(entityClass);
        }
    }

    /**
     * Check if an object is null or not and throw {@link AlreadyExistsException} with the message if
     * it is not null.
     *
     * @param object the object to check
     * @param message the error message
     */
    public static void checkExists(Object object, String message) {
        if (object != null) {
            throw new AlreadyExistsException(message);
        }
    }

}
