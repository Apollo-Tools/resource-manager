package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

/**
 * Implements methods to perform CRUD operations on the resource_provider entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class ResourceProviderChecker extends EntityChecker {

    private final ResourceProviderService resourceProviderService;

    /**
     * Create an instance from the resourceProviderService.
     *
     * @param resourceProviderService the resource provider service
     */
    public ResourceProviderChecker(ResourceProviderService resourceProviderService) {
        super(resourceProviderService);
        this.resourceProviderService = resourceProviderService;
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsOneByProvider = resourceProviderService
            .existsOneByProvider(entity.getString("provider"));
        return ErrorHandler.handleDuplicates(existsOneByProvider).ignoreElement();
    }
}
