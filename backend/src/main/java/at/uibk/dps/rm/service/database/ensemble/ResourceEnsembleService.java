package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.repository.ensemble.ResourceEnsembleRepository;
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
public interface ResourceEnsembleService extends DatabaseServiceInterface {
    @Generated
    @GenIgnore
    static ResourceEnsembleService create(ResourceEnsembleRepository repository) {
        return new ResourceEnsembleServiceImpl(repository);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ResourceEnsembleService createProxy(Vertx vertx) {
        return new ResourceEnsembleServiceVertxEBProxy(vertx,
            ServiceProxyAddress.getServiceProxyAddress(ResourceEnsemble.class));
    }

    Future<JsonArray> findAllByEnsembleId(long ensembleId);
}
