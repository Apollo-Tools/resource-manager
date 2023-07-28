package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.exception.BadInputException;
import io.reactivex.rxjava3.core.Completable;
import lombok.experimental.UtilityClass;

@UtilityClass
public class EntityNameValidator {
    public Completable checkName(String name, Class<?> entityClass) {
        if (name.matches("^[a-z0-9]+$")) {
            return Completable.complete();
        }
        return Completable.error(new BadInputException("invalid " + entityClass.getSimpleName() + " name"));
    }
}
