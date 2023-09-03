package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.dto.metric.MetricTypeEnum;
import at.uibk.dps.rm.entity.model.PlatformMetric;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.repository.metric.PlatformMetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.MetricValueUtility;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #MetricValueService.
 *
 * @author matthi-g
 */
@Deprecated
public class MetricValueServiceImpl extends DatabaseServiceProxy<MetricValue> implements MetricValueService{

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
    public Future<Void> saveAllToResource(long resourceId, JsonArray data) {
        CompletionStage<Void> createAll = withTransaction(session ->
            session.find(Resource.class, resourceId)
                .thenCompose(resource -> {
                    ServiceResultValidator.checkFound(resource, Resource.class);
                    return CompletableFuture.allOf(metricValueUtility.checkAddMetricList(session, resource, data)
                        .toArray(CompletableFuture[]::new));
                })
        );
        return sessionToFuture(createAll);
    }

    @Override
    public Future<JsonObject> findOne(long id) {
        CompletionStage<MetricValue> findOne = withSession(session -> repository.findByIdAndFetch(session, id));
        return Future.fromCompletionStage(findOne)
                .map(JsonObject::mapFrom);
    }

    @Override
    public Future<JsonArray> findAllByResource(long resourceId, boolean includeValue) {
        CompletionStage<List<MetricValue>> findAll = withSession(session ->
            session.find(Resource.class, resourceId)
                .thenCompose(resource -> {
                    ServiceResultValidator.checkFound(resource, Resource.class);
                    return repository.findByResourceAndFetch(session, resourceId);
                })
        );
        return sessionToFuture(findAll).map(result -> {
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
        });
    }

    @Override
    public Future<Void> updateByResourceAndMetric(long resourceId, long metricId, String valueString,
            Double valueNumber, Boolean valueBool, boolean isExternalSource) {
        CompletionStage<MetricValue> update = withTransaction(session ->
            repository.findByResourceAndMetricAndFetch(session, resourceId, metricId)
                .thenCompose(metricValue -> {
                    ServiceResultValidator.checkFound(metricValue, MetricValue.class);
                    return platformMetricRepository.findByResourceAndMetric(session, resourceId, metricId)
                        .thenApply(platformMetric -> {
                            ServiceResultValidator.checkFound(platformMetric, PlatformMetric.class);
                            if (platformMetric.getIsMonitored() && isExternalSource) {
                                throw new BadInputException("monitored metrics can't be updated manually");
                            }
                            MetricTypeEnum metricType = MetricTypeEnum
                                .fromMetricType(metricValue.getMetric().getMetricType());
                            if (!metricValueUtility.metricTypeMatchesValue(metricType, valueString) &&
                                !metricValueUtility.metricTypeMatchesValue(metricType, valueNumber) &&
                                !metricValueUtility.metricTypeMatchesValue(metricType, valueBool)) {
                                throw new BadInputException("invalid metric type");
                            }

                            metricValue.setValueString(valueString);
                            if (valueNumber!= null) {
                                metricValue.setValueNumber(valueNumber);
                            }
                            metricValue.setValueBool(valueBool);
                            return metricValue;
                    });
                })
        );
        return sessionToFuture(update).mapEmpty();
    }

    @Override
    public Future<Void> deleteByResourceAndMetric(long resourceId, long metricId) {
        CompletionStage<Integer> delete = withTransaction(session ->
            repository.findByResourceAndMetric(session, resourceId, metricId)
                .thenCompose(metricValue -> {
                    ServiceResultValidator.checkFound(metricValue, MetricValue.class);
                    return repository.deleteByResourceAndMetric(session, resourceId, metricId);
                })
        );
        return sessionToFuture(delete).mapEmpty();
    }
}
