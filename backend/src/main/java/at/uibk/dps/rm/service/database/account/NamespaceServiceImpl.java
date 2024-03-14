package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.exception.NotFoundException;
import at.uibk.dps.rm.repository.account.NamespaceRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.util.misc.RxVertxHandler;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

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
            SessionManagerProvider smProvider) {
        super(repository, K8sNamespace.class, smProvider);
        this.repository = repository;
        this.resourceRepository = resourceRepository;
    }

    @Override
    public void findAll(Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<K8sNamespace>> findAll = smProvider.withTransactionSingle(repository::findAllAndFetch);
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    @Override
    public void findAllByAccountId(long accountId, Handler<AsyncResult<JsonArray>> resultHandler) {
        Single<List<K8sNamespace>> findAll = smProvider.withTransactionSingle(sm -> repository
            .findAllByAccountIdAndFetch(sm, accountId));
        RxVertxHandler.handleSession(findAll.map(this::mapResultListToJsonArray), resultHandler);
    }

    @Override
    public void updateAllClusterNamespaces(String clusterName, List<String> namespaces,
            Handler<AsyncResult<Void>> resultHandler) {
        Completable updateAll = smProvider.withTransactionCompletable(sm -> resourceRepository
            .findClusterByName(sm, clusterName)
            .switchIfEmpty(Maybe.error(new NotFoundException("cluster " + clusterName + " is not registered")))
            .flatMapCompletable(resource -> repository.findAllByClusterName(sm, clusterName)
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
                    Object[] removeNamespaces = existingNamespaces.stream()
                        .filter(existingNamespace -> namespaces.stream()
                            .noneMatch(namespace -> namespace.equals(existingNamespace.getNamespace()))
                        )
                        .toArray();
                    Completable persist = newNamespaces.length > 0 ?
                        sm.persist(newNamespaces) : Completable.complete();
                    Completable remove = removeNamespaces.length > 0 ?
                        sm.remove(removeNamespaces) : Completable.complete();
                    return persist.andThen(remove);
                }))
        );
        RxVertxHandler.handleSession(updateAll, resultHandler);
    }
}
