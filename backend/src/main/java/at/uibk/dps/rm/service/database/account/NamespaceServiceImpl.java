package at.uibk.dps.rm.service.database.account;

import at.uibk.dps.rm.entity.model.K8sNamespace;
import at.uibk.dps.rm.repository.account.NamespaceRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.hibernate.reactive.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * This is the implementation of the #PlatformService.
 *
 * @author matthi-g
 */
public class NamespaceServiceImpl extends DatabaseServiceProxy<K8sNamespace> implements NamespaceService {

    private final NamespaceRepository repository;
    /**
     * Create an instance from the platformRepository.
     *
     * @param repository the platform repository
     */
    public NamespaceServiceImpl(NamespaceRepository repository, Stage.SessionFactory sessionFactory) {
        super(repository, K8sNamespace.class, sessionFactory);
        this.repository = repository;
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
}
