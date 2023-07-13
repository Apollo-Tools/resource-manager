package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #RegionService.
 *
 * @author matthi-g
 */
public class RegionServiceImpl extends DatabaseServiceProxy<Region> implements RegionService {
    private final RegionRepository repository;

    private final ResourceProviderRepository providerRepository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the region repository
     */
    public RegionServiceImpl(RegionRepository repository, ResourceProviderRepository providerRepository,
            Stage.SessionFactory sessionFactory) {
        super(repository, Region.class, sessionFactory);
        this.repository = repository;
        this.providerRepository = providerRepository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<Region> findOne = withSession(session -> repository.findByIdAndFetch(session, id));
        return Future.fromCompletionStage(findOne)
            .map(result -> {
                if (result != null) {
                    result.getResourceProvider().setProviderPlatforms(null);
                }
                return JsonObject.mapFrom(result);
            });
    }

    @Override
    public Future<JsonArray> findAll() {
        CompletionStage<List<Region>> findAll = withSession(repository::findAllAndFetch);
        return Future.fromCompletionStage(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (Region entity: result) {
                    entity.getResourceProvider().setProviderPlatforms(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }

    // TODO: check user role
    @Override
    public Future<JsonObject> save(JsonObject data) {
        Region region = data.mapTo(Region.class);
        CompletionStage<Region> create = withTransaction(session ->
            providerRepository.findByIdAndFetch(session, region.getResourceProvider().getProviderId())
                .thenCompose(provider -> {
                    ServiceResultValidator.checkFound(provider, ResourceProvider.class);
                    region.setResourceProvider(provider);
                    return repository.findOneByNameAndProviderId(session, region.getName(), provider.getProviderId());
                })
                .thenApply(existingRegion -> {
                    ServiceResultValidator.checkExists(existingRegion, Region.class);
                    session.persist(region);
                    return region;
                })
        );
        return transactionToFuture(create).map(result -> {
            result.getResourceProvider().setProviderPlatforms(null);
            result.getResourceProvider().setEnvironment(null);
            return JsonObject.mapFrom(result);
        });
    }

    @Override
    public Future<JsonArray> findAllByProviderId(long providerId) {
        CompletionStage<List<Region>> findAll = withSession(session ->
            repository.findAllByProviderId(session, providerId));
        return Future.fromCompletionStage(findAll)
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
        CompletionStage<Region> findOne = withSession(session ->
            repository.findOneByNameAndProviderId(session, name, providerId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }

    @Override
    public Future<JsonArray> findAllByPlatformId(long platformId) {
        CompletionStage<List<Region>> findAll = withSession(session ->
            repository.findAllByPlatformId(session, platformId));
        return Future.fromCompletionStage(findAll)
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
        CompletionStage<Region> findOne = withSession(session ->
            repository.findByRegionIdAndPlatformId(session, regionId, platformId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }
}
