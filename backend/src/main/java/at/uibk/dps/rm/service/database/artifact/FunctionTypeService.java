package at.uibk.dps.rm.service.database.artifact;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.FunctionType;
import at.uibk.dps.rm.repository.artifact.FunctionTypeRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * The interface of the service proxy for the artifact type entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface FunctionTypeService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static FunctionTypeService create(FunctionTypeRepository functionRepository, SessionManagerProvider smProvider) {
        return new FunctionTypeServiceImpl(functionRepository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static FunctionTypeService createProxy(Vertx vertx) {
        return new FunctionTypeServiceVertxEBProxy(vertx,
                ServiceProxyAddress.getServiceProxyAddress(FunctionType.class));
    }
}
