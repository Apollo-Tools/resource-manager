package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.handler.monitoring.K8sMonitoringHandler;
import at.uibk.dps.rm.handler.monitoring.MonitoringHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.rxjava3.core.AbstractVerticle;

import java.util.*;

/**
 * All monitoring processes of the resource manager are executed on the ApiVerticle.
 *
 * @author matthi-g
 */
public class MonitoringVerticle extends AbstractVerticle {

    private final Set<MonitoringHandler> monitoringHandlers = new HashSet<>();


    @Override
    public Completable rxStart() {
        ConfigDTO config = config().mapTo(ConfigDTO.class);
        monitoringHandlers.add(new K8sMonitoringHandler(vertx, config));
        return startMonitoringLoops();
    }

    private Completable startMonitoringLoops() {
        return Observable.fromIterable(monitoringHandlers)
            .flatMapCompletable(handler -> Completable.fromAction(handler::startMonitoringLoop));
    }
}
