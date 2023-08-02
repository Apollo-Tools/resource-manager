package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.repository.account.NamespaceRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import org.hibernate.reactive.stage.Stage;

/**
 * The interface of the service proxy for the platform entity.
 *
 * @author matthi-g
 */
@ProxyGen
@VertxGen
public interface NamespaceService extends DatabaseServiceInterface {

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    @GenIgnore
    static NamespaceService create(NamespaceRepository namespaceRepository, Stage.SessionFactory sessionFactory) {
        return new NamespaceServiceImpl(namespaceRepository, sessionFactory);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static NamespaceService createProxy(Vertx vertx) {
        return new NamespaceServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(K8sNamespace.class));
    }
}
