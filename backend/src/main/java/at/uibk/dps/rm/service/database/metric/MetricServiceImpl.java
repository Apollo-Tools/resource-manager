package at.uibk.dps.rm.service.database.metric;

import at.uibk.dps.rm.entity.dto.SLORequest;
import at.uibk.dps.rm.entity.dto.slo.ServiceLevelObjective;
import at.uibk.dps.rm.entity.model.Metric;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.metric.MetricRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.database.util.SLOUtility;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the implementation of the {@link MetricService}.
 *
 * @author matthi-g
 */
public class MetricServiceImpl extends DatabaseServiceProxy<Metric> implements MetricService {

    private final MetricRepository repository;

    /**
     * Create an instance from the metricRepository.
     *
     * @param repository the metric repository
     */
    public MetricServiceImpl(MetricRepository repository, SessionManagerProvider smProvider) {
        super(repository, Metric.class, smProvider);
        this.repository = repository;
    }

    @Override
    public void checkMetricTypeForSLOs(JsonObject request, Handler<AsyncResult<Void>> resultHandler) {
        SLORequest sloRequest = request.mapTo(SLORequest.class);
        List<ServiceLevelObjective> serviceLevelObjectives = sloRequest.getServiceLevelObjectives();
        Completable checkSLOs = smProvider.withTransactionCompletable(sm -> repository
            .findAllBySLOs(sm, serviceLevelObjectives)
            .flatMapObservable(Observable::fromIterable)
            .collect(Collectors.toMap(Metric::getMetric, metric -> metric))
            .flatMap(metrics -> Observable.fromIterable(sloRequest.getServiceLevelObjectives())
                .filter(slo -> metrics.containsKey(slo.getName()))
                .map(slo -> {
                    Metric metric = metrics.get(slo.getName());
                    SLOUtility.validateSLOType(slo, metric);
                    return slo;
                })
                .count()
            )
            .flatMapCompletable(matchingMetrics -> {
                if(matchingMetrics != serviceLevelObjectives.size()) {
                    return Completable.error(new NotFoundException("request contains unknown metric"));
                }
                return Completable.complete();
            })
        );
        RxVertxHandler.handleSession(checkSLOs, resultHandler);
    }
}
