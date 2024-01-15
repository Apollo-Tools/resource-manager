package at.uibk.dps.rm.service.monitoring.metricquery;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.dto.config.ConfigDTO;
import at.uibk.dps.rm.entity.monitoring.victoriametrics.VmResult;
import at.uibk.dps.rm.service.ServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.rxjava3.ext.web.client.WebClient;

import java.util.List;

@ProxyGen
@VertxGen
public interface MetricQueryService extends ServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static MetricQueryService create(WebClient webClient, ConfigDTO config) {
        return new MetricQueryServiceImpl(webClient, config);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static MetricQueryService createProxy(Vertx vertx) {
        return new MetricQueryServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(
            "metric-collector"));
    }

    void collectInstantMetric(String query, Handler<AsyncResult<List<VmResult>>> resultHandler);
}
