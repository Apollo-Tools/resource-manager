package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.exception.MonitoringException;
import at.uibk.dps.rm.repository.account.NamespaceRepository;
import at.uibk.dps.rm.repository.resource.ResourceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage.SessionFactory;

import java.util.*;
import java.util.concurrent.CompletionStage;

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
    public Future<JsonArray> findAll() {
        CompletionStage<List<K8sNamespace>> findAll = withSession(repository::findAllAndFetch);
        return sessionToFuture(findAll)
            .map(namespaces -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (K8sNamespace namespace: namespaces) {
                    objects.add(JsonObject.mapFrom(namespace));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<JsonArray> findAllByAccountId(long accountId) {
        CompletionStage<List<K8sNamespace>> findAll =
            withSession(session -> repository.findAllByAccountIdAndFetch(session, accountId));
        return sessionToFuture(findAll)
            .map(namespaces -> {
                ArrayList<JsonObject> objects = new ArrayList<>();
                for (K8sNamespace namespace: namespaces) {
                    objects.add(JsonObject.mapFrom(namespace));
                }
                return new JsonArray(objects);
            });
    }

    @Override
    public Future<Void> updateAllClusterNamespaces(String clusterName, List<String> namespaces) {
        CompletionStage<Void> updateAll = withTransaction(session ->
            resourceRepository.findClusterByName(session, clusterName)
                .thenCompose(resource -> {
                    if (resource == null) {
                        throw new MonitoringException("cluster " + clusterName + " is not registered");
                    }
                    return repository.findAllByClusterName(session, clusterName)
                        .thenCompose(existingNamespaces -> {
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
                            return session.persist(newNamespaces)
                                .thenCompose(res -> session.remove(deleteNamespaces));
                        });
                })
                .thenAccept(res -> {})
        );

        return sessionToFuture(updateAll);
    }
}
