package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.exception.BadInputException;
import io.reactivex.rxjava3.core.Completable;
import lombok.experimental.UtilityClass;

/**
 * This utility class is used to have a global validator for any name fields.
 *
 * @author matthi-g
 */
@UtilityClass
public class EntityNameValidator {
    /**
     * Check if a name fulfills the naming constraints.
     *
     * @param name the name
     * @param entityClass the class of the object with the name field
     * @return a successful Completable if the name is valid else a BadInputException is thrown
     */
    public Completable checkName(String name, Class<?> entityClass) {
        if (name.matches("^[a-z0-9]+$")) {
            return Completable.complete();
        }
        return Completable.error(new BadInputException("invalid " + entityClass.getSimpleName() + " name"));
    }
}
