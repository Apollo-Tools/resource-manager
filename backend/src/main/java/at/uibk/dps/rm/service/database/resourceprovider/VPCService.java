package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.VPC;
import at.uibk.dps.rm.repository.resourceprovider.VPCRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * The interface of the service proxy for the vpc entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface VPCService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static VPCService create(SessionManagerProvider smProvider) {
        return new VPCServiceImpl(new VPCRepository(), smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static VPCService createProxy(Vertx vertx) {
        return new VPCServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(VPC.class));
    }
}
