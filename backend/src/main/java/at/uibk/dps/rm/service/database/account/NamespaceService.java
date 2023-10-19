package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.annotations.Generated;
import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.repository.account.NamespaceRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.ServiceProxyAddress;
import at.uibk.dps.rm.service.database.DatabaseServiceInterface;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

import java.util.List;

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
    static NamespaceService create(NamespaceRepository namespaceRepository, SessionManagerProvider smProvider) {
        return new NamespaceServiceImpl(namespaceRepository, new ResourceRepository(), smProvider);
    }

    @SuppressWarnings("PMD.CommentRequired")
    @Generated
    static NamespaceService createProxy(Vertx vertx) {
        return new NamespaceServiceVertxEBProxy(vertx, ServiceProxyAddress.getServiceProxyAddress(K8sNamespace.class));
    }

    /**
     * Update all namespaces for a cluster resource.
     *
     * @param clusterName the name of the cluster resource
     * @param namespaces the list of namespaces
     * @param resultHandler receives nothing if the update was successful else an error
     */
    void updateAllClusterNamespaces(String clusterName, List<String> namespaces,
        Handler<AsyncResult<Void>> resultHandler);
}
