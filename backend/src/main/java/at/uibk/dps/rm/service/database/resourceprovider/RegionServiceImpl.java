package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This is the implementation of the #RegionService.
 *
 * @author matthi-g
 */
public class RegionServiceImpl extends DatabaseServiceProxy<Region> implements RegionService {
    private final RegionRepository regionRepository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the region repository
     */
    public RegionServiceImpl(RegionRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, Region.class, sessionFactory);
        this.regionRepository = repository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(regionRepository.findByIdAndFetch(id))
            .map(result -> {
                if (result != null) {
                    result.getResourceProvider().setProviderPlatforms(null);
                }
                return JsonObject.mapFrom(result);
            });
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(regionRepository.findAllAndFetch())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Region entity: result) {
                    entity.getResourceProvider().setProviderPlatforms(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonArray> findAllByProviderId(long providerId) {
        return Future
            .fromCompletionStage(regionRepository.findAllByProviderId(providerId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Region entity: result) {
                    entity.setResourceProvider(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Boolean> existsOneByNameAndProviderId(String name, long providerId) {
        return Future
            .fromCompletionStage(regionRepository.findOneByNameAndProviderId(name, providerId))
            .map(Objects::nonNull);
    }

    @Override
    public Future<JsonArray> findAllByPlatformId(long platformId) {
        return Future
            .fromCompletionStage(regionRepository.findAllByPlatformId(platformId))
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Region entity: result) {
                    entity.getResourceProvider().setProviderPlatforms(null);
                    entity.getResourceProvider().setEnvironment(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Boolean> existsByPlatformId(long regionId, long platformId) {
        return Future
            .fromCompletionStage(regionRepository.findByRegionIdAndPlatformId(regionId, platformId))
            .map(Objects::nonNull);
    }
}
