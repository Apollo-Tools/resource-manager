package at.uibk.dps.rm.rx.service.database.metric;

import at.uibk.dps.rm.entity.dto.metric.MetricTypeEnum;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.rx.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.rx.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.rx.service.database.util.MetricValueUtility;
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
        Completable createAll = withTransactionCompletable(sessionManager -> sessionManager
            .find(Resource.class, resourceId)
            .switchIfEmpty(Maybe.error(new NotFoundException(Resource.class)))
            .flatMapCompletable(resource -> metricValueUtility.checkAddMetricList(sessionManager, resource, data)
                .flatMapCompletable(Completable::merge)
            )
        );
        handleSession(createAll, resultHandler);
    }

    @Override
    public void findOne(long id, Handler<AsyncResult<JsonObject>> resultHandler) {
        Maybe<MetricValue> findOne = withTransactionMaybe(session -> repository.findByIdAndFetch(session, id)
            .switchIfEmpty(Maybe.error(new NotFoundException(MetricValue.class))));
        handleSession(findOne.map(JsonObject::mapFrom), resultHandler);
    }

    @Override
    public void findAllByResource(long resourceId, boolean includeValue, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<MetricValue>> findAll = withTransactionSingle(sessionManager -> repository
            .findAllByResourceAndFetch(sessionManager, resourceId));
        handleSession(
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
        Completable update = withTransactionCompletable(sessionManager -> repository
            .findByResourceAndMetricAndFetch(sessionManager, resourceId, metricId)
            .switchIfEmpty(Maybe.error(new NotFoundException(MetricValue.class)))
            .flatMapCompletable(metricValue -> platformMetricRepository
                .findByResourceAndMetric(sessionManager, resourceId, metricId)
                .switchIfEmpty(Maybe.error(new NotFoundException(PlatformMetric.class)))
                .flatMapCompletable(platformMetric -> {
                    if (platformMetric.getIsMonitored() && isExternalSource) {
                        return Completable.error(new BadInputException("monitored metrics can't be updated manually"));
                    }
                    MetricTypeEnum metricType = MetricTypeEnum
                        .fromMetricType(metricValue.getMetric().getMetricType());
                    if (!metricValueUtility.metricTypeMatchesValue(metricType, valueString) &&
                        !metricValueUtility.metricTypeMatchesValue(metricType, valueNumber) &&
                        !metricValueUtility.metricTypeMatchesValue(metricType, valueBool)) {
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
        handleSession(update, resultHandler);
    }

    @Override
    public void deleteByResourceAndMetric(long resourceId, long metricId, Handler<AsyncResult<Void>> resultHandler) {
        Completable delete = withTransactionCompletable(sessionManager -> repository
            .findByResourceAndMetric(sessionManager, resourceId, metricId)
            .switchIfEmpty(Maybe.error(new NotFoundException(MetricValue.class)))
            .flatMapCompletable(sessionManager::remove)
        );
        handleSession(delete, resultHandler);
    }
}
