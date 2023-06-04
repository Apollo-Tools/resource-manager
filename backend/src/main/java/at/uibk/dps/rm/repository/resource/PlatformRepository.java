package at.uibk.dps.rm.repository.resource;

import at.uibk.dps.rm.entity.model.Platform;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the platform entity.
 *
 * @author matthi-g
 */
public class PlatformRepository extends Repository<Platform> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public PlatformRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Platform.class);
    }

    /**
     * Find all platforms and fetch the resource types.
     *
     * @return a CompletionStage that emits a list of all platforms
     */
    public CompletionStage<List<Platform>> findAllAndFetch() {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct p from Platform p " +
                    "left join fetch p.resourceType", entityClass)
                .getResultList()
        );
    }
}
