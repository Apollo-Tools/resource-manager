package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.handler.ValidationHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.RegionService;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.ResourceProviderService;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava3.ext.web.RoutingContext;

public class RegionHandler extends ValidationHandler {

    private final ResourceProviderChecker providerChecker;

    public RegionHandler(RegionService regionService, ResourceProviderService providerService) {
        super(new RegionChecker(regionService));
        providerChecker = new ResourceProviderChecker(providerService);
    }

    @Override
    public Single<JsonObject> postOne(RoutingContext rc) {
        JsonObject requestBody = rc.body().asJsonObject();
        return providerChecker.checkExistsOne(requestBody
                .getJsonObject("resource_provider")
                .getLong("provider_id"))
            .andThen(entityChecker.checkForDuplicateEntity(requestBody))
            .andThen(Single.defer(() -> Single.just(1L)))
            .flatMap(result -> entityChecker.submitCreate(requestBody));
    }
}
