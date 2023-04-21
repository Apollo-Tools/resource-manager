package at.uibk.dps.rm.repository.function;

import at.uibk.dps.rm.entity.model.Runtime;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the runtime entity.
 *
 * @author matthi-g
 */
public class RuntimeRepository extends Repository<Runtime> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public RuntimeRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Runtime.class);
    }

    public CompletionStage<Runtime> findByName(String name) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from Runtime where name=:name", entityClass)
                .setParameter("name", name)
                .getSingleResultOrNull()
        );
    }
}
