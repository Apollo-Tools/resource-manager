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
import at.uibk.dps.rm.service.database.util.SessionManager;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

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

    private final MetricValueUtility metricValueUtility;

    /**
     * Create an instance from the metricValueRepository.
     *
     * @param repository the metric value repository
     */
    public MetricValueServiceImpl(MetricValueRepository repository, PlatformMetricRepository platformMetricRepository,
            SessionFactory sessionFactory) {
        super(repository, MetricValue.class, sessionFactory);
        this.repository = repository;
        this.platformMetricRepository = platformMetricRepository;
        this.metricValueUtility = new MetricValueUtility(repository, platformMetricRepository);
    }

    @Override
    public void saveAllToResource(long resourceId, JsonArray data, Handler<AsyncResult<Void>> resultHandler) {
        Completable createAll = SessionManager.withTransactionCompletable(sessionFactory, sm -> sm
            .find(Resource.class, resourceId)
            .switchIfEmpty(Maybe.error(new NotFoundException(Resource.class)))
            .flatMapCompletable(resource -> metricValueUtility.checkAddMetricList(sm, resource, data))
        );
        RxVertxHandler.handleSession(createAll, resultHandler);
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<MetricValue> findOne = SessionManager.withTransactionMaybe(sessionFactory, sm -> repository
            .findByIdAndFetch(sm, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(MetricValue.class))));
        RxVertxHandler.handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAllByResource(long resourceId, boolean includeValue, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<MetricValue>> findAll = SessionManager.withTransactionSingle(sessionFactory, sm -> repository
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
            Double valueNumber, Boolean valueBool, boolean isExternalSource, Handler<AsyncResult<Void>> resultHandler) {
        Completable update = SessionManager.withTransactionCompletable(sessionFactory, sm -> repository
            .findByResourceAndMetricAndFetch(sm, resourceId, metricId)
            .switchIfEmpty(Maybe.error(new NotFoundException(MetricValue.class)))
            .flatMapCompletable(metricValue -> platformMetricRepository
                .findByResourceAndMetric(sm, resourceId, metricId)
                .switchIfEmpty(Maybe.error(new NotFoundException(PlatformMetric.class)))
                .flatMapCompletable(platformMetric -> {
                    if (platformMetric.getIsMonitored() && isExternalSource) {
                        return Completable.error(new BadInputException("monitored metrics can't be updated manually"));
                    }
                    MetricTypeEnum metricType = MetricTypeEnum
                        .fromMetricType(metricValue.getMetric().getMetricType());
                    if (!MetricValueUtility.metricTypeMatchesValue(metricType, valueString) &&
                        !MetricValueUtility.metricTypeMatchesValue(metricType, valueNumber) &&
                        !MetricValueUtility.metricTypeMatchesValue(metricType, valueBool)) {
                        return Completable.error(new BadInputException("invalid metric type"));
                    }
                    metricValue.setValueString(valueString);
                    if (valueNumber!= null) {
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
        Completable delete = SessionManager.withTransactionCompletable(sessionFactory, sm -> repository
            .findByResourceAndMetric(sm, resourceId, metricId)
            .switchIfEmpty(Maybe.error(new NotFoundException(MetricValue.class)))
            .flatMapCompletable(sm::remove)
        );
        RxVertxHandler.handleSession(delete, resultHandler);
    }
}
