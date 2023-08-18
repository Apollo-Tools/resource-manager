package at.uibk.dps.rm.repository.artifact;

import at.uibk.dps.rm.entity.model.FunctionType;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the function artifact type entity.
 *
 * @author matthi-g
 */
public class FunctionTypeRepository extends Repository<FunctionType> {
    /**
     * Create an instance.
     */
    public FunctionTypeRepository() {
        super(FunctionType.class);
    }

    /**
     * Find a function type by its name
     *
     * @param session the database session
     * @param name the name of the function type
     * @return a CompletionStage that emits the function type if it exists, else null
     */
    public CompletionStage<FunctionType> findByName(Session session, String name) {
        return session.createQuery(
        "from FunctionType ft " +
                "where ft.name =:name", entityClass)
            .setParameter("name", name)
            .getSingleResultOrNull();
    }
}
