package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public class VPCServiceImpl extends ServiceProxy<VPC> implements VPCService {
    private final VPCRepository vpcRepository;

    public VPCServiceImpl(VPCRepository vpcRepository) {
        super(vpcRepository, VPC.class);
        this.vpcRepository = vpcRepository;
    }

    public Future<JsonObject> findOneByRegionIdAndAccountId(long regionId, long accountId) {
        return Future
            .fromCompletionStage(vpcRepository.findByRegionIdAndAccountId(regionId, accountId))
            .map(result -> {
                if (result != null) {
                    result.setCreatedBy(null);
                    result.setRegion(null);
                }
                return JsonObject.mapFrom(result);
            });
    }
}
