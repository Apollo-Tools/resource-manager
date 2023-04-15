package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Objects;

public class VPCServiceImpl extends ServiceProxy<VPC> implements VPCService {
    private final VPCRepository vpcRepository;

    public VPCServiceImpl(VPCRepository vpcRepository) {
        super(vpcRepository, VPC.class);
        this.vpcRepository = vpcRepository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(vpcRepository.findByIdAndFetch(id))
            .map(result -> {
                if (result != null) {
                    result.setCreatedBy(null);
                }
                return JsonObject.mapFrom(result);
            });
    }

    @Override
    public Future<JsonArray> findAll() {
        return Future
            .fromCompletionStage(vpcRepository.findAllAndFetch())
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (VPC entity: result) {
                    entity.setCreatedBy(null);
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            });
    }


    @Override
    public Future<JsonObject> findOneByRegionIdAndAccountId(long regionId, long accountId) {
        return Future
            .fromCompletionStage(vpcRepository.findByRegionIdAndAccountId(regionId, accountId))
            .map(result -> {
                if (result != null) {
                    result.setCreatedBy(null);
                }
                return JsonObject.mapFrom(result);
            });
    }

    @Override
    public Future<Boolean> existsOneByRegionIdAndAccountId(long regionId, long accountId) {
        return Future
            .fromCompletionStage(vpcRepository.findByRegionIdAndAccountId(regionId, accountId))
            .map(Objects::nonNull);
    }
}
