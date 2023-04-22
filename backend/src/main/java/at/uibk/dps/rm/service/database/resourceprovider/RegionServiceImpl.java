package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

public class RegionServiceImpl extends DatabaseServiceProxy<Region> implements RegionService {
    private final RegionRepository regionRepository;

    public RegionServiceImpl(RegionRepository repository) {
        super(repository, Region.class);
        this.regionRepository = repository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(regionRepository.findByIdAndFetch(id))
            .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(regionRepository.findAllAndFetch())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Region entity: result) {
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
}
