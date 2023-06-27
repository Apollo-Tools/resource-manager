package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.Objects;

/**
 * This is the implementation of the #VPCService.
 *
 * @author matthi-g
 */
public class VPCServiceImpl extends DatabaseServiceProxy<VPC> implements VPCService {
    private final VPCRepository vpcRepository;

    /**
     * Create an instance from the vpcRepository.
     *
     * @param vpcRepository the vpc repository
     */
    public VPCServiceImpl(VPCRepository vpcRepository, Stage.SessionFactory sessionFactory) {
        super(vpcRepository, VPC.class, sessionFactory);
        this.vpcRepository = vpcRepository;
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        return Future
            .fromCompletionStage(vpcRepository.findByIdAndFetch(id))
            .map(result -> {
                if (result != null) {
                    result.getRegion().getResourceProvider().setProviderPlatforms(null);
                    result.getRegion().getResourceProvider().setEnvironment(null);
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
                    entity.getRegion().getResourceProvider().setProviderPlatforms(null);
                    entity.getRegion().getResourceProvider().setEnvironment(null);
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
                    result.getRegion().getResourceProvider().setProviderPlatforms(null);
                    result.getRegion().getResourceProvider().setEnvironment(null);
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
