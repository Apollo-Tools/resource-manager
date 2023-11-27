package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.AwsPrice;
import at.uibk.dps.rm.repository.resourceprovider.AwsPriceRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;

/**
 * The interface of the service proxy for the environment entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface AwsPriceService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static AwsPriceService create(AwsPriceRepository awsPriceRepository, SessionManagerProvider smProvider) {
        return new AwsPriceServiceImpl(awsPriceRepository, smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static AwsPriceService createProxy(Vertx vertx) {
        return new AwsPriceServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(AwsPrice.class));
    }
}
