package at.uibk.dps.rm.repository.service;

import at.uibk.dps.rm.entity.model.ServiceType;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

/**
 * Implements database operations for the service_type entity.
 *
 * @author matthi-g
 */
public class ServiceTypeRepository extends Repository<ServiceType> {
    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public ServiceTypeRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ServiceType.class);
    }
}
