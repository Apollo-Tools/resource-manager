package at.uibk.dps.rm.util.validation;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A utility class that can be used to validate the items of a collection.
 *
 * @author matthi-g
 */
@UtilityClass
public class CollectionValidator {

    /**
     * Check a collection if it contains duplicates.
     *
     * @param collection the collection of items
     * @param <T> the type of items
     * @return a Completable
     */
    public static <T> Completable hasDuplicates(Collection<T> collection) {
        return Maybe.just(collection)
            .mapOptional(items -> {
                Set<T> uniques = new HashSet<>();

                for (T t : collection) {
                    if (!uniques.add(t)) {
                        throw new Throwable("duplicated input");
                    }
                }
                return Optional.empty();
            }).ignoreElement();
    }
}
