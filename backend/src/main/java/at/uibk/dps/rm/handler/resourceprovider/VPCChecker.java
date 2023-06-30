package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.entity.dto.resource.PlatformEnum;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.VPCService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implements methods to perform CRUD operations on the vpc entity.
 *
 * @see EntityChecker
 *
 * @author matthi-g
 */
public class VPCChecker extends EntityChecker {

    private final VPCService vpcService;

    /**
     * Create an instance from the vpcService.
     *
     * @param vpcService the vpce service
     */
    public VPCChecker(VPCService vpcService) {
        super(vpcService);
        this.vpcService = vpcService;
    }

    public Single<JsonObject> submitCreate(long accountId, JsonObject requestBody) {
        return vpcService.saveToAccount(accountId, requestBody);
    }

    public Completable submitDelete(long accountId, long vpcId) {
        return vpcService.deleteFromAccount(accountId, vpcId);
    }

    /**
     * @see #checkForDuplicateEntity(JsonObject)
     */
    public Completable checkForDuplicateEntity(JsonObject entity, long accountId) {
        Single<Boolean> existsOneByResourceType = vpcService
            .existsOneByRegionIdAndAccountId(entity.getJsonObject("region").getLong("region_id"), accountId);
        return ErrorHandler.handleDuplicates(existsOneByResourceType).ignoreElement();
    }

    /**
     * Find one vpc by its region and creator.
     *
     * @param regionId the id of the region
     * @param accountId the id of the creator
     * @return a Single that emits the vpc as JsonObject
     */
    public Single<JsonObject> checkFindOneByRegionIdAndAccountId(long regionId, long accountId) {
        Single<JsonObject> findOneByRegionIdAndAccountId = vpcService.findOneByRegionIdAndAccountId(regionId,
            accountId);
        return ErrorHandler.handleFindOne(findOneByRegionIdAndAccountId);
    }

    /**
     * Find all vpc that were registered by the account and are necessary for the resources to be
     * deployed.
     *
     * @param accountId the id of the account
     * @param resources the list of resources
     * @return a Single that emits a list of found vpcs
     */
    public Single<List<JsonObject>> checkVPCForFunctionResources(long accountId,
            JsonArray resources) {
        List<Single<JsonObject>> singles = new ArrayList<>();
        HashSet<Long> regionIds = new HashSet<>();
        for (Object object: resources) {
            Resource resource = ((JsonObject) object).mapTo(Resource.class);
            long regionId = resource.getRegion().getRegionId();
            String platform = resource.getPlatform().getPlatform();
            if (!regionIds.contains(regionId) && platform.equals(PlatformEnum.EC2.getValue())) {
                singles.add(this.checkFindOneByRegionIdAndAccountId(regionId, accountId));
                regionIds.add(regionId);
            }
        }
        if (singles.isEmpty()) {
            return Single.just(new ArrayList<>());
        }
        return Single.zip(singles, objects -> Arrays.stream(objects).map(object -> (JsonObject) object)
            .collect(Collectors.toList()));
    }
}
