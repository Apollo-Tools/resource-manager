package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.repository.account.NamespaceRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.service.util.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the implementation of the #PlatformService.
 *
 * @author matthi-g
 */
public class NamespaceServiceImpl extends DatabaseServiceProxy<K8sNamespace> implements NamespaceService {

    private final NamespaceRepository repository;

    private final ResourceRepository resourceRepository;

    /**
     * Create an instance from the platformRepository.
     *
     * @param repository the platform repository
     */
    public NamespaceServiceImpl(NamespaceRepository repository, ResourceRepository resourceRepository,
            SessionFactory sessionFactory) {
        super(repository, K8sNamespace.class, sessionFactory);
        this.repository = repository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<K8sNamespace>> findAll = withTransactionSingle(repository::findAllAndFetch);
        RxVertxHandler.handleSession(
            findAll
                .map(namespaces -> {
                    ArrayList<JsonObject> objects = new ArrayList<>();
                    for (K8sNamespace namespace: namespaces) {
                        objects.add(JsonObject.mapFrom(namespace));
                    }
                    return new JsonArray(objects);
                }),
            resultHandler
        );
    }

    @Override
    public void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<K8sNamespace>> findAll = withTransactionSingle(sessionManager -> repository
            .findAllByAccountIdAndFetch(sessionManager, accountId));
        RxVertxHandler.handleSession(
            findAll
                .map(namespaces -> {
                    ArrayList<JsonObject> objects = new ArrayList<>();
                    for (K8sNamespace namespace: namespaces) {
                        objects.add(JsonObject.mapFrom(namespace));
                    }
                    return new JsonArray(objects);
                }),
            resultHandler
        );
    }

    @Override
    public void updateAllClusterNamespaces(String clusterName, List<String> namespaces,
            Handler<AsyncResult<Void>> resultHandler) {
        Completable updateAll = withTransactionCompletable(sessionManager -> resourceRepository
            .findClusterByName(sessionManager, clusterName)
            // TODO: handle not found exception in Monitoring verticle and throw as monitoring exception
            .switchIfEmpty(Maybe.error(new MonitoringException("cluster " + clusterName + " is not registered")))
            .flatMapCompletable(resource -> repository.findAllByClusterName(sessionManager, clusterName)
                .flatMapCompletable(existingNamespaces -> {
                    Object[] newNamespaces = namespaces.stream()
                        .filter(namespace -> existingNamespaces.stream()
                            .noneMatch(existingNamespace -> existingNamespace.getNamespace().equals(namespace))
                        )
                        .map(namespace -> {
                            K8sNamespace newNamespace = new K8sNamespace();
                            newNamespace.setNamespace(namespace);
                            newNamespace.setResource(resource);
                            return newNamespace;
                        })
                        .toArray();
                    Object[] deleteNamespaces = existingNamespaces.stream()
                        .filter(existingNamespace -> namespaces.stream()
                            .noneMatch(namespace -> namespace.equals(existingNamespace.getNamespace()))
                        )
                        .toArray();
                    return sessionManager.persist(newNamespaces)
                        .andThen(sessionManager.remove(deleteNamespaces));
                }))
        );
        RxVertxHandler.handleSession(updateAll, resultHandler);
    }
}
