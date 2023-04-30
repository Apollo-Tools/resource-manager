package at.uibk.dps.rm.handler;

import at.uibk.dps.rm.exception.*;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * This class is used for Validation of the results in extensions of the {@link EntityChecker}
 *
 * @author matthi-g
 */
//TODO: change return value of Single<Boolean> to Completable
public class ErrorHandler {

  /**
   * Check if the result of a findOne method contains an entity.
   *
   * @param handler the result to validate
   * @return the handler if the item that the Single emits is not null, else throws a
   * {@link NotFoundException}
   */
  public static Single<JsonObject> handleFindOne(final Single<JsonObject> handler) {
        return handler
            .map(result -> {
                if (result == null) {
                    throw new NotFoundException();
                }
                return result;
            });
    }

  /**
   * Check if the result of a findAll method contains an entities.
   *
   * @param handler the result to validate
   * @return the handler if the item that the Single emits is not null, else throws a
   * {@link NotFoundException}
   */
    public static Single<JsonArray> handleFindAll(final Single<JsonArray> handler) {
        return handler
                .map(result -> {
                    if (result == null) {
                        throw new NotFoundException();
                    }
                    return result;
                });
    }

  /**
   * Check if the result of a existsOne method is true.
   *
   * @param handler the result to validate
   * @return the result if the item that the Single emits is true, else throws a
   * {@link NotFoundException}
   */
    public static Single<Boolean> handleExistsOne(final Single<Boolean> handler) {
        return handler
            .map(result -> {
                if (!result) {
                    throw new NotFoundException();
                }
                return true;
            });
    }

  /**
   * Check if the result of a existsOne method is false.
   *
   * @param handler the result to validate
   * @return the result if the item that the Single emits is false, else throws a
   * {@link AlreadyExistsException}
   */
    public static Single<Boolean> handleDuplicates(final Single<Boolean> handler) {
        return handler
            .map(result -> {
                if (result) {
                    throw new AlreadyExistsException();
                }
                return false;
            });
    }

  /**
   * Check if the result of a existsOne method is false.
   *
   * @param handler the result to validate
   * @return the result if the item that the Single emits is true, else throws a
   * {@link UsedByOtherEntityException}
   */
  public static Single<Boolean> handleUsedByOtherEntity(final Single<Boolean> handler) {
        return handler
            .map(result -> {
                if (result) {
                    throw new UsedByOtherEntityException();
                }
                return false;
            });
    }

  /**
   * Check if the result of a checkInput method is true.
   *
   * @param handler the result to validate
   * @return the result if the item that the Single emits is true, else throws a
   * {@link BadInputException}
   */
    public static Single<Boolean> handleBadInput(final Single<Boolean> handler) {
        return handler
            .map(result -> {
                if (!result) {
                    throw new BadInputException();
                }
                return true;
            });
    }

  /**
   * Check if the result of a login method contains the account entity.
   *
   * @param handler the result to validate
   * @return the result if the item that the Single emits is not null, else throws a
   * {@link UnauthorizedException}
   */
    public static Single<JsonObject> handleLoginCredentials(final Single<JsonObject> handler) {
        return handler
            .map(result -> {
                if (result == null) {
                    throw new UnauthorizedException();
                }
                return result;
            });
    }

  /**
   * Check if the result of a credentialsExist method is true.
   *
   * @param handler the result to validate
   * @return the result if the item that the Single emits is true, else throws a
   * {@link UnauthorizedException}
   */
    public static Single<Boolean> handleCredentialsExist(final Single<Boolean> handler) {
        return handler
            .map(result -> {
                if (!result) {
                    throw new UnauthorizedException();
                }
                return true;
            });
    }

  /**
   * Check if the result of a missingRequiredMetrics method is true.
   *
   * @param handler the result to validate
   * @return the result if the item that the Single emits is false, else throws a
   * {@link NotFoundException}
   */
    public static Single<Boolean> handleMissingRequiredMetrics(final Single<Boolean> handler) {
        return handler
            .map(result -> {
                if (result) {
                    throw new NotFoundException();
                }
                return false;
            });
    }

  /**
   * Check if the result of a checkTemplateHasContent method is not null or blank.
   *
   * @param handler the result to validate
   * @return the result if the item that the Single emits not null or blank, else throws a
   * {@link NotFoundException}
   */
    public static Single<String> handleTemplateHasContent(final Single<String> handler) {
        return handler
            .map(result -> {
                if (result == null || result.isBlank()) {
                    throw new NotFoundException();
                }
                return result;
            });
    }
}
