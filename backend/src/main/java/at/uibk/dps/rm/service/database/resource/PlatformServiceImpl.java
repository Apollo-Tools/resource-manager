package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.repository.resource.PlatformRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.concurrent.CompletionStage;

public class PlatformServiceImpl extends DatabaseServiceProxy<Platform> implements PlatformService {

    private final PlatformRepository repository;
    /**
     * Create an instance from the platformRepository.
     *
     * @param repository the platform repository
     */
    public PlatformServiceImpl(PlatformRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, Platform.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Platform> findOne = getSessionFactory().withSession(session ->
            repository.findById(session, id));
        return Future.fromCompletionStage(findOne)
            .map(platform -> {
                if (platform != null) {
                    platform.setResourceType(null);
                }
                return JsonObject.mapFrom(platform);
            });
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(repository.findAllAndFetch())
            .map(platforms -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Platform platform: platforms) {
                    objects.add(JsonObject.mapFrom(platform));
                }
                return new JsonArray(objects);
            });
    }
}
