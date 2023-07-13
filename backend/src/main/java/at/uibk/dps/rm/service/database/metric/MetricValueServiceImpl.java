package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.dto.metric.MetricTypeEnum;
import at.uibk.dps.rm.entity.model.Resource;
import at.uibk.dps.rm.exception.BadInputException;
import at.uibk.dps.rm.repository.metric.MetricValueRepository;
import at.uibk.dps.rm.entity.model.MetricValue;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.validation.ServiceResultValidator;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * This is the implementation of the #MetricValueService.
 *
 * @author matthi-g
 */
public class MetricValueServiceImpl extends DatabaseServiceProxy<MetricValue> implements MetricValueService{

    private final MetricValueRepository repository;

    /**
     * Create an instance from the metricValueRepository.
     *
     * @param repository the metric value repository
     */
    public MetricValueServiceImpl(MetricValueRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, MetricValue.class, sessionFactory);
        this.repository = repository;
    }

    @Override
    public Future<Void> saveAll(JsonArray data) {
        List<MetricValue> metricValues = data
            .stream()
            .map(object -> {
                JsonObject metricValueJson = (JsonObject) object;
                Resource resource = metricValueJson.getJsonObject("resource").mapTo(Resource.class);
                MetricValue metricValue = metricValueJson.mapTo(MetricValue.class);
                metricValue.setResource(resource);
                return metricValue;
            })
            .collect(Collectors.toList());
        CompletionStage<Void> createAll = withTransaction(session -> repository.createAll(session, metricValues));
        return transactionToFuture(createAll);
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
            repository.findByResourceAndFetch(session, resourceId));
        return Future.fromCompletionStage(findAll)
            .map(result -> {
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
    public Future<Boolean> existsOneByResourceAndMetric(long resourceId, long metricId) {
        CompletionStage<MetricValue> findOne = withSession(session ->
            repository.findByResourceAndMetric(session, resourceId, metricId));
        return Future.fromCompletionStage(findOne)
            .map(Objects::nonNull);
    }

    @Override
    public Future<Void> updateByResourceAndMetric(long resourceId, long metricId, String valueString,
            Double valueNumber, Boolean valueBool, boolean isExternalSource) {
        CompletionStage<MetricValue> update = withTransaction(session ->
            repository.findByResourceAndMetricAndFetch(session, resourceId, metricId)
                .thenApply(metricValue -> {
                    ServiceResultValidator.checkFound(metricValue, MetricValue.class);
                    if (metricValue.getMetric().getIsMonitored() && isExternalSource) {
                        throw new BadInputException("monitored metrics can't be updated manually");
                    }
                    MetricTypeEnum metricType = MetricTypeEnum.fromMetricType(metricValue.getMetric().getMetricType());
                    if (!metricTypeMatchesValue(metricType, valueString) &&
                        !metricTypeMatchesValue(metricType, valueNumber) &&
                        !metricTypeMatchesValue(metricType, valueBool)) {
                        throw new BadInputException("invalid metric type");
                    }

                    metricValue.setValueString(valueString);
                    if (valueNumber!= null) {
                        metricValue.setValueNumber(valueNumber);
                    }
                    metricValue.setValueBool(valueBool);
                    return metricValue;
                })
        );
        return transactionToFuture(update).mapEmpty();
    }

    @Override
    public Future<Void> deleteByResourceAndMetric(long resourceId, long metricId) {
        CompletionStage<Integer> delete = withTransaction(session ->
            repository.deleteByResourceAndMetric(session, resourceId, metricId));
        return Future.fromCompletionStage(delete)
            .mapEmpty();
    }

    /**
     * Check if a value is a string and if the metric type is string as well.
     *
     * @param metricType the metric type
     * @param value the value
     * @return true if the types match, else false
     */
    private boolean metricTypeMatchesValue(MetricTypeEnum metricType, String value) {
        return value!=null && metricType.equals(MetricTypeEnum.STRING);
    }

    /**
     * @see #metricTypeMatchesValue(MetricTypeEnum metricType, String value)
     */
    private boolean metricTypeMatchesValue(MetricTypeEnum metricType, Double value) {
        return value!=null && metricType.equals(MetricTypeEnum.NUMBER);
    }

    /**
     * @see #metricTypeMatchesValue(MetricTypeEnum metricType, String value)
     */
    private boolean metricTypeMatchesValue(MetricTypeEnum metricType, Boolean value) {
        return value!=null && metricType.equals(MetricTypeEnum.BOOLEAN);
    }
}
