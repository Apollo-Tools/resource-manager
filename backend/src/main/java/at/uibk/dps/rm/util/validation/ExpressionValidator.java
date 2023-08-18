package at.uibk.dps.rm.util.validation;

import at.uibk.dps.rm.entity.dto.slo.ExpressionType;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import lombok.experimental.UtilityClass;

import java.util.Optional;

/**
 * Utility class to check if expressions from service level objectives are valid.
 *
 * @author matthi-g
 */
@UtilityClass
public class ExpressionValidator {
    /**
     * Check whether the expressions of the slos are valid.
     *
     * @param slos the service level objectives
     * @return a Completable
     */
    public static Completable checkExpressionsAreValid(JsonArray slos) {
        if (slos == null) {
            return Completable.complete();
        }
        return Single.just(slos)
            .map(items -> {
                for (int i = 0; i < items.size(); i++) {
                    if (!ExpressionType.symbolExists(items.getJsonObject(i).getString("expression"))) {
                        throw new Throwable("expression is not supported");
                    }
                }
                return Optional.empty();
            })
            .ignoreElement();
    }
}
