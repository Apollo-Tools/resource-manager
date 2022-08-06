package at.uibk.dps.rm.handler.property;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.property.PropertyValueService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;

public class PropertyValueChecker extends EntityChecker {

    private final PropertyValueService propertyValueService;

    public PropertyValueChecker(PropertyValueService propertyValueService) {
        super(propertyValueService);
        this.propertyValueService = propertyValueService;
    }

    public Single<JsonArray> checkFindAllByResource(long id) {
        return propertyValueService.findAllByResource(id);
    }

    public Completable submitDeletePropertyValue(long resourceId, long propertyId) {
        return propertyValueService.deleteByResourceAndProperty(resourceId, propertyId);
    }

    public Completable checkForDuplicateByResourceAndProperty(long resourceId, long propertyId) {
        Single<Boolean> existsOneByResourceAndProperty = propertyValueService.existsOneByResourceAndProperty(resourceId, propertyId);
        return ErrorHandler.handleDuplicates(existsOneByResourceAndProperty).ignoreElement();
    }

    public Completable checkPropertyValueExistsByResourceAndProperty(long resourceId, long metricId) {
        Single<Boolean> existsOneByResourceAndProperty =
                propertyValueService.existsOneByResourceAndProperty(resourceId, metricId);
        return ErrorHandler.handleExistsOne(existsOneByResourceAndProperty).ignoreElement();
    }
}
