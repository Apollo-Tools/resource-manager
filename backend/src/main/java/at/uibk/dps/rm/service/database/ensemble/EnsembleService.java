package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface EnsembleService extends DatabaseServiceInterface {
    @Generated
    @GenIgnore
    static EnsembleService create(EnsembleRepository ensembleRepository) {
        return new EnsembleServiceImpl(ensembleRepository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static EnsembleService createProxy(Vertx vertx) {
        return new EnsembleServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(Ensemble.class));
    }

    Future<JsonArray> findAllByAccountId(long accountId);

    Future<JsonObject> findOneByIdAndAccountId(long id, long accountId);

    Future<Boolean> existsOneByNameAndAccountId(String name, long accountId);

    Future<Void> updateEnsembleValidity(long ensembleId, boolean isValid);
}
