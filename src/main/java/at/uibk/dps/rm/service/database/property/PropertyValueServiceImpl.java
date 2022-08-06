package at.uibk.dps.rm.service.database.property;

import at.uibk.dps.rm.repository.property.PropertyValueRepository;
import at.uibk.dps.rm.repository.property.entity.PropertyValue;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PropertyValueServiceImpl extends ServiceProxy<PropertyValue> implements PropertyValueService {

    private final PropertyValueRepository propertyValueRepository;

    public PropertyValueServiceImpl(PropertyValueRepository propertyValueRepository) {
        super(propertyValueRepository, PropertyValue.class);
        this.propertyValueRepository = propertyValueRepository;
    }

    @Override
    public Future<Void> saveAll(JsonArray data) {
        List<PropertyValue> propertyValues = data
            .stream()
            .map(object -> ((JsonObject) object).mapTo(PropertyValue.class))
            .collect(Collectors.toList());

        return Future
            .fromCompletionStage(propertyValueRepository.createAll(propertyValues));
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
                .fromCompletionStage(propertyValueRepository.findByIdAndFetch(id))
                .map(result -> {
                    if (result != null) {
                        result.setResource(null);
                    }
                    return JsonObject.mapFrom(result);
                });
    }

    @Override
    public Future<JsonArray> findAllByResource(long resourceId) {
        return Future
            .fromCompletionStage(propertyValueRepository.findByResourceAndFetch(resourceId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (PropertyValue propertyValue: result) {
                    objects.add(JsonObject.mapFrom(propertyValue.getProperty()));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Boolean> existsOneByResourceAndProperty(long resourceId, long propertyId) {
        return Future
            .fromCompletionStage(propertyValueRepository.findByResourceAndProperty(resourceId, propertyId))
            .map(Objects::nonNull);
    }

    @Override
    public Future<Void> deleteByResourceAndProperty(long resourceId, long propertyId) {
        return Future
            .fromCompletionStage(propertyValueRepository.deleteByResourceAndProperty(resourceId, propertyId))
            .mapEmpty();
    }
}
