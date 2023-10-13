package at.uibk.dps.rm.repository.artifact;

import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;

/**
 * Implements database operations for the service artifact type entity.
 *
 * @author matthi-g
 */
public class ServiceTypeRepository extends Repository<ServiceType> {
    /**
     * Create an instance.
     */
    public ServiceTypeRepository() {
        super(ServiceType.class);
    }

    /**
     * Find a service type by its name
     *
     * @param sessionManager the database session manager
     * @param name the name of the service type
     * @return a Maybe that emits the service type if it exists, else null
     */
    public Maybe<ServiceType> findByName(SessionManager sessionManager, String name) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from ServiceType st where st.name =:name", entityClass)
            .setParameter("name", name)
            .getSingleResultOrNull()
        );
    }
}
