package at.uibk.dps.rm.rx.service.database.artifact;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.rx.repository.artifact.ServiceTypeRepository;
import at.uibk.dps.rm.rx.service.ServiceProxyAddress;
import at.uibk.dps.rm.rx.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import org.hibernate.reactive.stage.Stage.SessionFactory;

/**
 * The interface of the service proxy for the artifact type entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface ServiceTypeService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static ServiceTypeService create(ServiceTypeRepository serviceTypeRepository, SessionFactory sessionFactory) {
        return new ServiceTypeServiceImpl(serviceTypeRepository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static ServiceTypeService createProxy(Vertx vertx) {
        return new ServiceTypeServiceVertxEBProxy(vertx,
                ServiceProxyAddress.getServiceProxyAddress(ServiceType.class));
    }
}
