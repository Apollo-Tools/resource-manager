package at.uibk.dps.rm.repository.artifact;

import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;

import java.util.concurrent.CompletionStage;

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
     * @param session the database session
     * @param name the name of the service type
     * @return a CompletionStage that emits the service type if it exists, else null
     */
    public CompletionStage<ServiceType> findByName(Session session, String name) {
        return session.createQuery(
        "from ServiceType st " +
                "where st.name =:name", entityClass)
            .setParameter("name", name)
            .getSingleResultOrNull();
    }
}
