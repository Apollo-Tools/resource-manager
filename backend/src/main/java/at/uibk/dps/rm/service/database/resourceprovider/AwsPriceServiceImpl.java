package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.AwsPrice;
import at.uibk.dps.rm.repository.resourceprovider.AwsPriceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * This is the implementation of the {@link AwsPriceService}.
 *
 * @author matthi-g
 */
public class AwsPriceServiceImpl extends DatabaseServiceProxy<AwsPrice> implements AwsPriceService {

    private final AwsPriceRepository awsPriceRepository;

    /**
     * Create an instance from the repository.
     *
     * @param repository the region repository
     */
    public AwsPriceServiceImpl(AwsPriceRepository repository, SessionManagerProvider smProvider) {
        super(repository, AwsPrice.class, smProvider);
        this.awsPriceRepository = repository;
    }

    @Override
    public void saveAll(JsonArray data, Handler<AsyncResult<Void>> resultHandler) {
        Completable createAll = smProvider.withTransactionCompletable(sm ->
            Observable.fromIterable(data)
                .map(entry -> ((JsonObject) entry).mapTo(AwsPrice.class))
                .flatMapSingle(price -> awsPriceRepository.findByRegionIdPlatformIdAndInstanceType(sm,
                        price.getRegion().getRegionId(), price.getPlatform().getPlatformId(), price.getInstanceType())
                    .switchIfEmpty(Single.just(price))
                    .map(foundPrice -> {
                        foundPrice.setPrice(price.getPrice());
                        return foundPrice;
                    }))
                .toList()
                .flatMapCompletable(awsPrices -> sm.persist(awsPrices.toArray()))
        );
        RxVertxHandler.handleSession(createAll, resultHandler);
    }
}
