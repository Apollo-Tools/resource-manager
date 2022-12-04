package at.uibk.dps.rm.handler.cloudprovider;

import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.cloudprovider.CloudProviderService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;

public class CloudProviderChecker extends EntityChecker {

    private final CloudProviderService cloudProviderService;

    public CloudProviderChecker(CloudProviderService cloudProviderService) {
        super(cloudProviderService);
        this.cloudProviderService = cloudProviderService;
    }

    @Override
    public Completable checkForDuplicateEntity(JsonObject entity) {
        Single<Boolean> existsOneByProvider = cloudProviderService
            .existsOneByProvider(entity.getString("provider"));
        return ErrorHandler.handleDuplicates(existsOneByProvider).ignoreElement();
    }
}
