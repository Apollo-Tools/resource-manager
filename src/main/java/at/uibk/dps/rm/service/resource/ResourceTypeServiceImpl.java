package at.uibk.dps.rm.service.resource;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.repository.resource.entity.ResourceType;
import at.uibk.dps.rm.service.database.DatabaseService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceProxyBuilder;
import org.hibernate.reactive.stage.Stage.SessionFactory;

public class ResourceTypeServiceImpl implements ResourceTypeService {
    private Repository<ResourceType> resourceTypeRepository;

    private final DatabaseService databaseService;

    public ResourceTypeServiceImpl(Vertx vertx) {
        ServiceProxyBuilder builder = new ServiceProxyBuilder(vertx)
                .setAddress("database-service-address");
        databaseService = builder.build(DatabaseService.class);
    }

    @Override
    public Future<Void> save(JsonObject resourceType) {
        return null;
    }

    @Override
    public Future<JsonObject> findOne(int id) {
        return null;
    }

    @Override
    public Future<JsonArray> findAll() {
        return databaseService.findAll("ResourceType");
    }
}
