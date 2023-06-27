package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;

/**
 * This is the implementation of the #ResourceProviderService.
 *
 * @author matthi-g
 */
public class ResourceProviderServiceImpl extends DatabaseServiceProxy<ResourceProvider> implements ResourceProviderService {

    private final ResourceProviderRepository repository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the resource provider repository
     */
    public ResourceProviderServiceImpl(ResourceProviderRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, ResourceProvider.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(repository.findByIdAndFetch(id))
            .map(obj -> {
                if (obj != null) {
                    obj.setProviderPlatforms(null);
                }
                return JsonObject.mapFrom(obj);
            });
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(repository.findAllAndFetch())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (ResourceProvider entity: result) {
                    entity.setProviderPlatforms(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }
}
