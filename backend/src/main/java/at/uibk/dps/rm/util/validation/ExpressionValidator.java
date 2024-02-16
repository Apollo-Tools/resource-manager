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
                    String expressionSymbol = items.getJsonObject(i).getString("expression");
                    JsonArray value = items.getJsonObject(i).getJsonArray("value");
                    if (!ExpressionType.symbolExists(expressionSymbol)) {
                        throw new Throwable("expression is not supported");
                    }
                    ExpressionType expressionType = ExpressionType.fromString(expressionSymbol);
                    if (!expressionType.equals(ExpressionType.EQ) && value.size() != 1) {
                        throw new Throwable("expression type only supports single value");
                    }
                    Object firstValue = value.getValue(0);
                    if (!expressionType.equals(ExpressionType.EQ) && (firstValue instanceof Boolean ||
                        firstValue instanceof String)) {
                        throw new Throwable("expression does not support value type");
                    }
                }
                return Optional.empty();
            })
            .ignoreElement();
    }
}
