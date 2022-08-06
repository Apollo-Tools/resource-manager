package at.uibk.dps.rm.handler.property;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.handler.resource.ResourceChecker;
import at.uibk.dps.rm.repository.property.entity.Property;
import at.uibk.dps.rm.repository.property.entity.PropertyValue;
import at.uibk.dps.rm.repository.resource.entity.Resource;
import at.uibk.dps.rm.service.rxjava3.database.property.PropertyService;
import at.uibk.dps.rm.service.rxjava3.database.property.PropertyValueService;
import at.uibk.dps.rm.service.rxjava3.database.resource.ResourceService;
import at.uibk.dps.rm.util.HttpHelper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PropertyValueHandler extends ValidationHandler {

    private final PropertyValueChecker propertyValueChecker;

    private final PropertyChecker propertyChecker;

    private final ResourceChecker resourceChecker;

    public PropertyValueHandler(PropertyValueService propertyValueService,
                                PropertyService propertyService, ResourceService resourceService) {
        super(new PropertyValueChecker(propertyValueService));
        propertyValueChecker = (PropertyValueChecker) super.entityChecker;
        propertyChecker = new PropertyChecker(propertyService);
        resourceChecker = new ResourceChecker(resourceService);
    }

    @Override
    protected Single<JsonArray> getAll(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "id")
                .flatMap(id -> resourceChecker.checkExistsOne(id)
                        .andThen(Single.just(id)))
                .flatMap(propertyValueChecker::checkFindAllByResource);
    }

    @Override
    public Completable postAll(RoutingContext rc) {
        JsonArray requestBody = rc.body().asJsonArray();
        return HttpHelper.getLongPathParam(rc, "id")
                .flatMap(id -> resourceChecker.checkExistsOne(id)
                        .andThen(Single.just(id)))
                .flatMap(id -> checkAddPropertiesResourceExists(requestBody, id))
                .flatMapCompletable(propertyValues -> propertyValueChecker.submitCreateAll(Json.encodeToBuffer(propertyValues).toJsonArray()));
    }

    @Override
    public Completable deleteOne(RoutingContext rc) {
        return HttpHelper.getLongPathParam(rc, "resourceId")
                .flatMap(resourceId -> HttpHelper.getLongPathParam(rc, "propertyId")
                        .map(propertyId -> Map.of("resourceId", resourceId, "propertyId", propertyId))
                        .flatMap(ids -> checkDeletePropertyValueExists(ids.get("resourceId"), ids.get("propertyId"))
                                .andThen(Single.just(ids))
                        ))
                .flatMapCompletable(ids -> propertyValueChecker.submitDeletePropertyValue(ids.get("resourceId"), ids.get("propertyId")));
    }

    private Single<List<PropertyValue>> checkAddPropertiesResourceExists(JsonArray requestBody, long resourceId) {
        List<PropertyValue> propertyValues = new ArrayList<>();
        List<Completable> completables = checkAddPropertyList(requestBody, resourceId, propertyValues);
        return Completable.merge(completables)
                .andThen(Single.just(propertyValues));
    }

    private List<Completable> checkAddPropertyList(JsonArray requestBody, long resourceId, List<PropertyValue> propertyValues) {
        Resource resource = new Resource();
        resource.setResourceId(resourceId);
        List<Completable> completables = new ArrayList<>();
        requestBody.stream().forEach(jsonObject -> {
            JsonObject jsonProperty = (JsonObject) jsonObject;
            long propertyId = jsonProperty.getLong("propertyId");
            Property property = new Property();
            property.setPropertyId(propertyId);
            PropertyValue propertyValue = new PropertyValue();
            propertyValue.setResource(resource);
            propertyValue.setProperty(property);
            propertyValues.add(propertyValue);
            completables.add(propertyChecker.checkExistsOne(propertyId));
            completables.add(propertyValueChecker.checkForDuplicateByResourceAndProperty(resourceId, propertyId));
        });
        return completables;
    }

    private Completable checkDeletePropertyValueExists(long resourceId, long propertyId) {
        return Completable.mergeArray(
                resourceChecker.checkExistsOne(resourceId),
                propertyChecker.checkExistsOne(propertyId),
                propertyValueChecker.checkPropertyValueExistsByResourceAndProperty(resourceId, propertyId));
    }
}
