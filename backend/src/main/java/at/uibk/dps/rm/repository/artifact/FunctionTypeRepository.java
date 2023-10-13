package at.uibk.dps.rm.repository.artifact;

import at.uibk.dps.rm.entity.model.FunctionType;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;

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
     * @param sessionManager the database session manager
     * @param name the name of the function type
     * @return a Maybe that emits the function type if it exists, else null
     */
    public Maybe<FunctionType> findByName(SessionManager sessionManager, String name) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from FunctionType ft where ft.name =:name", entityClass)
            .setParameter("name", name)
            .getSingleResultOrNull()
        );
    }
}
