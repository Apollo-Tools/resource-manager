package at.uibk.dps.rm.handler.property;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.property.PropertyService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class PropertyChecker extends EntityChecker {

    private final PropertyService propertyService;

    public PropertyChecker(PropertyService propertyService) {
        super(propertyService);
        this.propertyService = propertyService;
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsOneByProperty = propertyService.existsOneByProperty(entity.getString("property"));
        return ErrorHandler.handleDuplicates(existsOneByProperty).ignoreElement();
    }

    @Override
    public Single<JsonObject> checkUpdateNoDuplicate(JsonObject requestBody, JsonObject entity) {
        if (requestBody.containsKey("property")) {
            return this.checkForDuplicateEntity(requestBody)
                    .andThen(Single.just(entity));
        }
        return Single.just(entity);
    }

    public Completable checkExistsOne(String property) {
        Single<Boolean> existsOneByProperty = propertyService.existsOneByProperty(property);
        return ErrorHandler.handleExistsOne(existsOneByProperty).ignoreElement();
    }
}
