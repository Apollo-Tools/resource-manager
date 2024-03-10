package at.uibk.dps.rm.verticle;

import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.handler.alerting.AlertingHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.rxjava3.core.AbstractVerticle;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.HashSet;
import java.util.Set;

/**
 * All alerting processes of the resource manager are executed on the AlertingVerticle.
 *
 * @author matthi-g
 */
public class AlertingVerticle extends AbstractVerticle {

    private static final Logger logger = LoggerFactory.getLogger(AlertingVerticle.class);

    private final Set<AlertingHandler> alertingHandlers = new HashSet<>();

    @Override
    public Completable rxStart() {
        WebClient webClient = WebClient.create(vertx);
        ConfigDTO config = config().mapTo(ConfigDTO.class);
        AlertingHandler alertingHandler = new AlertingHandler(vertx, webClient, config);
        alertingHandlers.add(alertingHandler);
        return startAlertingLoop();
    }

    private Completable startAlertingLoop() {
        return Observable.fromIterable(alertingHandlers)
            .flatMapCompletable(handler -> Completable.fromAction(handler::startValidationLoop))
            .doOnComplete(() -> logger.info("Started alerting loop"))
            .doOnError(throwable -> logger.error("Error", throwable));
    }
}
