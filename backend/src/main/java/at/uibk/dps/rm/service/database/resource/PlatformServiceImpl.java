package at.uibk.dps.rm.service.database.resource;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.repository.resource.PlatformRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

public class PlatformServiceImpl extends DatabaseServiceProxy<Platform> implements PlatformService {

    private final PlatformRepository platformRepository;
    /**
     * Create an instance from the platformRepository.
     *
     * @param platformRepository the platform repository
     */
    public PlatformServiceImpl(PlatformRepository platformRepository) {
        super(platformRepository, Platform.class);
        this.platformRepository = platformRepository;
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(platformRepository.findAllAndFetch())
            .map(platforms -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Platform platform: platforms) {
                    objects.add(JsonObject.mapFrom(platform));
                }
                return new JsonArray(objects);
            });
    }
}
