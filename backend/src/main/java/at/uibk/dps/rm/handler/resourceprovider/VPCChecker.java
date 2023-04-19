package at.uibk.dps.rm.handler.resourceprovider;

import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.handler.EntityChecker;
import at.uibk.dps.rm.handler.ErrorHandler;
import at.uibk.dps.rm.service.rxjava3.database.resourceprovider.VPCService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
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

    public VPCChecker(VPCService vpcService) {
        super(vpcService);
        this.vpcService = vpcService;
    }

    public Completable checkForDuplicateEntity(JsonObject entity, long accountId) {
        Single<Boolean> existsOneByResourceType = vpcService
            .existsOneByRegionIdAndAccountId(entity.getJsonObject("region").getLong("region_id"), accountId);
        return ErrorHandler.handleDuplicates(existsOneByResourceType).ignoreElement();
    }

    public Single<JsonObject> checkFindOneByRegionIdAndAccountId(long regionId, long accountId) {
        Single<JsonObject> findOneByRegionIdAndAccountId = vpcService.findOneByRegionIdAndAccountId(regionId,
            accountId);
        return ErrorHandler.handleFindOne(findOneByRegionIdAndAccountId);
    }

    public Single<List<JsonObject>> checkVPCForFunctionResources(long accountId,
        List<JsonObject> functionResources) {
        List<Single<JsonObject>> singles = new ArrayList<>();
        HashSet<Long> regionIds = new HashSet<>();
        for (JsonObject jsonObject: functionResources) {
            Region region = jsonObject.mapTo(FunctionResource.class).getResource().getRegion();
            long regionId = region.getRegionId();
            if (!regionIds.contains(regionId) && !region.getName().equals("edge")) {
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
