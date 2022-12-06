package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class ResourceProviderChecker extends EntityChecker {

    private final ResourceProviderService resourceProviderService;

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
