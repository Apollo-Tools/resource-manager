package at.uibk.dps.rm.util;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ArrayValidator<T> {

    public Completable hasDuplicates(Collection<T> collection) {
        return Maybe.just(collection)
            .mapOptional(items -> {
                Set<T> uniques = new HashSet<>();

                for (T t : collection) {
                    if (!uniques.add(t)) {
                        throw new Throwable("slo array contains duplicates");
                    }
                }
                return Optional.empty();
            }).ignoreElement();
    }
}
