package at.uibk.dps.rm.rx.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.rx.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.rx.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceProxy;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the implementation of the {@link VPCService}.
 *
 * @author matthi-g
 */
public class VPCServiceImpl extends DatabaseServiceProxy<VPC> implements VPCService {
    private final VPCRepository repository;

    private final RegionRepository regionRepository;

    /**
     * Create an instance from the vpcRepository.
     *
     * @param repository the vpc repository
     */
    public VPCServiceImpl(VPCRepository repository, RegionRepository regionRepository,
            Stage.SessionFactory sessionFactory) {
        super(repository, VPC.class, sessionFactory);
        this.repository = repository;
        this.regionRepository = regionRepository;
    }

    @Override
    public void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<VPC>> findAll = withTransactionSingle(sessionManager -> repository
            .findAllByAccountIdAndFetch(sessionManager, accountId));
        handleSession(
            findAll.map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (VPC entity: result) {
                    objects.add(JsonObject.mapFrom(entity));
                }
                return new JsonArray(objects);
            }),
            resultHandler
        );
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<VPC> findOne = withTransactionMaybe(sessionManager -> repository.findByIdAndFetch(sessionManager, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(VPC.class))));
        handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void saveToAccount(long accountId, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        VPC vpc = data.mapTo(VPC.class);
        Single<VPC> create = withTransactionSingle(sessionManager -> repository
            .findByRegionIdAndAccountId(sessionManager, vpc.getRegion().getRegionId(), accountId)
            .flatMap(existingVPC -> Maybe.<Region>error(new AlreadyExistsException(VPC.class)))
            .switchIfEmpty(regionRepository.findByIdAndFetch(sessionManager, vpc.getRegion().getRegionId()))
            .switchIfEmpty(Single.error(new NotFoundException(Region.class)))
            .flatMap(region -> {
                vpc.setRegion(region);
                Account account = new Account();
                account.setAccountId(accountId);
                vpc.setCreatedBy(account);
                return sessionManager.persist(vpc);
            })
        );
        handleSession(create.map(JsonObject::mapFrom), resultHandler);
    }
}
