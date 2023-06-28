package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #VPCService.
 *
 * @author matthi-g
 */
public class VPCServiceImpl extends DatabaseServiceProxy<VPC> implements VPCService {
    private final VPCRepository repository;

    /**
     * Create an instance from the vpcRepository.
     *
     * @param repository the vpc repository
     */
    public VPCServiceImpl(VPCRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, VPC.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<JsonArray> findAll() {
        CompletionStage<List<VPC>> findAll = withSession(repository::findAllAndFetch);
        return Future.fromCompletionStage(findAll)
            .map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (VPC entity: result) {
                    objects.add(serializeVPC(entity));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<VPC> findOne = withSession(session -> repository.findByIdAndFetch(session, id));
        return Future.fromCompletionStage(findOne)
            .map(this::serializeVPC);
    }


    @Override
    public Future<JsonObject> findOneByRegionIdAndAccountId(long regionId, long accountId) {
        CompletionStage<VPC> findOne = withSession(session ->
            repository.findByRegionIdAndAccountId(session, regionId, accountId));
        return Future.fromCompletionStage(findOne)
            .map(this::serializeVPC);
    }

    @Override
    public Future<Boolean> existsOneByRegionIdAndAccountId(long regionId, long accountId) {
        CompletionStage<VPC> findOne = withSession(session ->
            repository.findByRegionIdAndAccountId(session, regionId, accountId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }

    private JsonObject serializeVPC(VPC vpc) {
        if (vpc != null) {
            vpc.getRegion().getResourceProvider().setProviderPlatforms(null);
            vpc.getRegion().getResourceProvider().setEnvironment(null);
            vpc.setCreatedBy(null);
        }
        return JsonObject.mapFrom(vpc);
    }
}
