package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the region entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class RegionChecker extends EntityChecker {

    private final RegionService regionService;

    /**
     * Create an instance from the regionService.
     *
     * @param regionService the region service
     */
    public RegionChecker(RegionService regionService) {
        super(regionService);
        this.regionService = regionService;
    }

    /**
     * Find all regions by resource provider.
     *
     * @param providerId the id of the provider
     * @return a Single that emits all found regions as JsonArray
     */
    public Single<JsonArray> checkFindAllByProvider(long providerId) {
        Single<JsonArray> checkFindAllByProviderId = regionService.findAllByProviderId(providerId);
        return ErrorHandler.handleFindAll(checkFindAllByProviderId);
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsOneByNameAndProviderId = regionService
            .existsOneByNameAndProviderId(entity.getString("name"),
                entity.getJsonObject("resource_provider").getLong("provider_id"));
        return ErrorHandler.handleDuplicates(existsOneByNameAndProviderId).ignoreElement();
    }
}
