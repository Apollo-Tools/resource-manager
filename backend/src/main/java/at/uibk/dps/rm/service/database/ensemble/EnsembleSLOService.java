package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.EnsembleSLO;
import at.uibk.dps.rm.repository.ensemble.EnsembleSLORepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;

/**
 * The interface of the service proxy for the ensemble_slo entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface EnsembleSLOService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static EnsembleSLOService create(EnsembleSLORepository ensembleSLORepository) {
        return new EnsembleSLOServiceImpl(ensembleSLORepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static EnsembleSLOService createProxy(Vertx vertx) {
        return new EnsembleSLOServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(EnsembleSLO.class));
    }

    /**
     * Find all ensembleSLOs by their ensembleId.
     *
     * @param ensembleId the id of the ensemble
     * @return a Future that emits all ensembleSLOs as JsonArray
     */
    Future<JsonArray> findAllByEnsembleId(long ensembleId);
}
