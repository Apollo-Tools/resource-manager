package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.ensemble.EnsembleRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.util.ServiceProxyAddress;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

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
}
