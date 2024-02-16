package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.dto.metric.MetricTypeEnum;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.MetricValueUtility;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the implementation of the {@link MetricValueService}.
 *
 * @author matthi-g
 */
public class MetricValueServiceImpl extends DatabaseServiceProxy<MetricValue> implements MetricValueService {

    private final MetricValueRepository repository;

    private final PlatformMetricRepository platformMetricRepository;

    /**
     * Create an instance from the metricValueRepository.
     *
     * @param repository the metric value repository
     */
    public MetricValueServiceImpl(MetricValueRepository repository, PlatformMetricRepository platformMetricRepository,
            SessionManagerProvider smProvider) {
        super(repository, MetricValue.class, smProvider);
        this.repository = repository;
        this.platformMetricRepository = platformMetricRepository;
    }

    @Override
    public void saveAllToResource(long resourceId, JsonArray data, Handler<AsyncResult<Void>> resultHandler) {
        MetricValueUtility metricValueUtility = new MetricValueUtility(repository, platformMetricRepository);
        Completable createAll = smProvider.withTransactionCompletable(sm -> sm
            .find(Resource.class, resourceId)
            .switchIfEmpty(Maybe.error(new NotFoundException(Resource.class)))
            .flatMapCompletable(resource -> metricValueUtility.checkAddMetricList(sm, resource, data))
        );
        RxVertxHandler.handleSession(createAll, resultHandler);
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<MetricValue> findOne = smProvider.withTransactionMaybe(sm -> repository.findByIdAndFetch(sm, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(MetricValue.class))));
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAllByResource(long resourceId, boolean includeValue, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<MetricValue>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByResourceAndFetch(sm, resourceId));
        RxVertxHandler.handleSession(
            findAll.map(result -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (MetricValue metricValue: result) {
                    // necessary check because when no metric values are present the result list contains one null value
                    if (metricValue == null) {
                        continue;
                    }
                    JsonObject entity;
                    if (includeValue) {
                        entity = JsonObject.mapFrom(metricValue);
                    } else {
                        entity = JsonObject.mapFrom(metricValue.getMetric());
                    }
                    objects.add(entity);
                }
                return new JsonArray(objects);
            }),
            resultHandler
        );
    }

    @Override
    public void updateByResourceAndMetric(long resourceId, long metricId, String valueString,
            Double valueNumber, Boolean valueBool, Handler<AsyncResult<Void>> resultHandler) {
        Completable update = smProvider.withTransactionCompletable(sm -> repository
            .findByResourceAndMetricAndFetch(sm, resourceId, metricId)
            .switchIfEmpty(Maybe.error(new NotFoundException(MetricValue.class)))
            .flatMapCompletable(metricValue -> platformMetricRepository
                .findByResourceAndMetric(sm, resourceId, metricId)
                .switchIfEmpty(Maybe.error(new NotFoundException(PlatformMetric.class)))
                .flatMapCompletable(platformMetric -> {
                    if (platformMetric.getIsMonitored()) {
                        return Completable.error(new BadInputException("monitored metrics can't be updated"));
                    }
                    MetricTypeEnum metricType = MetricTypeEnum.fromMetricType(metricValue.getMetric().getMetricType());
                    if (!MetricValueUtility.metricTypeMatchesValue(metricType, valueString) &&
                        !MetricValueUtility.metricTypeMatchesValue(metricType, valueNumber) &&
                        !MetricValueUtility.metricTypeMatchesValue(metricType, valueBool)) {
                        return Completable.error(new BadInputException("invalid metric type"));
                    }
                    metricValue.setValueString(valueString);
                    if (valueNumber != null) {
                        metricValue.setValueNumber(valueNumber);
                    }
                    metricValue.setValueBool(valueBool);
                    return Completable.complete();
            }))
        );
        RxVertxHandler.handleSession(update, resultHandler);
    }

    @Override
    public void deleteByResourceAndMetric(long resourceId, long metricId, Handler<AsyncResult<Void>> resultHandler) {
        Completable delete = smProvider.withTransactionCompletable(sm -> repository
            .findByResourceAndMetric(sm, resourceId, metricId)
            .switchIfEmpty(Maybe.error(new NotFoundException(MetricValue.class)))
            .flatMapCompletable(sm::remove)
        );
        RxVertxHandler.handleSession(delete, resultHandler);
    }
}
