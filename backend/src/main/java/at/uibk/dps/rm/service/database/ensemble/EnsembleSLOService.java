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

@ProxyGen
@VertxGen
public interface EnsembleSLOService extends DatabaseServiceInterface {
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

    Future<JsonArray> findAll();

    Future<JsonArray> findAllByEnsembleId(long ensembleId);
}
