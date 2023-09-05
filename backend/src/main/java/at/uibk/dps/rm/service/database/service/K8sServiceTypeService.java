package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.K8sServiceType;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import at.uibk.dps.rm.repository.service.K8sServiceTypeRepository;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import org.hibernate.reactive.stage.Stage.SessionFactory;

/**
 * The interface of the service proxy for the service_type entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface K8sServiceTypeService extends DatabaseServiceInterface {
    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static K8sServiceTypeService create(K8sServiceTypeRepository k8sServiceTypeRepository,
            SessionFactory sessionFactory) {
        return new K8sServiceTypeServiceImpl(k8sServiceTypeRepository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static K8sServiceTypeService createProxy(Vertx vertx) {
        return new K8sServiceTypeServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(K8sServiceType.class));
    }
}
