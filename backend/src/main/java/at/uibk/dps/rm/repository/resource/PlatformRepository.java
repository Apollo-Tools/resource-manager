package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.repository.Repository;
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
     * @param session the database session
     * @return a CompletionStage that emits a list of all platforms
     */
    public CompletionStage<List<Platform>> findAllAndFetch(Session session) {
        return session.createQuery("select distinct p from Platform p " +
                "left join fetch p.resourceType", entityClass)
            .getResultList();
    }
}
