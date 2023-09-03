package at.uibk.dps.rm.rx.repository.resource;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.rx.repository.Repository;
import at.uibk.dps.rm.rx.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Single;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the platform entity.
 *
 * @author matthi-g
 */
public class PlatformRepository extends Repository<Platform> {

    /**
     * Create an instance.
     */
    public PlatformRepository() {
        super(Platform.class);
    }

    /**
     * Find all platforms and fetch the resource types.
     *
     * @param sessionManager the database session manager
     * @return a Single that emits a list of all platforms
     */
    public Single<List<Platform>> findAllAndFetch(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct p from Platform p " +
                "left join fetch p.resourceType", entityClass)
            .getResultList()
        );
    }
}
