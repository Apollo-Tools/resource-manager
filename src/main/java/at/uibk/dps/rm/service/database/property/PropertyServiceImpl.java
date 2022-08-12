package at.uibk.dps.rm.service.database.property;

import at.uibk.dps.rm.repository.PropertyRepository;
import at.uibk.dps.rm.entity.model.Property;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;

import java.util.Objects;

public class PropertyServiceImpl extends ServiceProxy<Property> implements PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyServiceImpl(PropertyRepository propertyRepository) {
        super(propertyRepository, Property.class);
        this.propertyRepository = propertyRepository;
    }

    @Override
    public Future<Boolean> existsOneByProperty(String property) {
        return Future
            .fromCompletionStage(propertyRepository.findByProperty(property))
            .map(Objects::nonNull);
    }
}
