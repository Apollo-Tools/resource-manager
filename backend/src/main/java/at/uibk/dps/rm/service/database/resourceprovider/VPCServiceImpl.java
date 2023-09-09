package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.Account;
import at.uibk.dps.rm.entity.model.Region;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.exception.AlreadyExistsException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.resourceprovider.RegionRepository;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
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
        Single<List<VPC>> findAll = SessionManager.withTransactionSingle(sessionFactory, sm -> repository
            .findAllByAccountIdAndFetch(sm, accountId));
        RxVertxHandler.handleSession(
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
        Maybe<VPC> findOne = SessionManager.withTransactionMaybe(sessionFactory, sm -> repository.findByIdAndFetch(sm, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(VPC.class))));
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void saveToAccount(long accountId, JsonObject data, Handler<AsyncResult<JsonObject>> resultHandler) {
        VPC vpc = data.mapTo(VPC.class);
        Single<VPC> create = SessionManager.withTransactionSingle(sessionFactory, sm -> repository
            .findByRegionIdAndAccountId(sm, vpc.getRegion().getRegionId(), accountId)
            .flatMap(existingVPC -> Maybe.<Region>error(new AlreadyExistsException(VPC.class)))
            .switchIfEmpty(regionRepository.findByIdAndFetch(sm, vpc.getRegion().getRegionId()))
            .switchIfEmpty(Single.error(new NotFoundException(Region.class)))
            .flatMap(region -> {
                vpc.setRegion(region);
                Account account = new Account();
                account.setAccountId(accountId);
                vpc.setCreatedBy(account);
                return sm.persist(vpc);
            })
        );
        RxVertxHandler.handleSession(create.map(JsonObject::mapFrom), resultHandler);
    }
}
