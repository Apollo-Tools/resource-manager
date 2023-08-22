package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.entity.model.K8sServiceType;
import at.uibk.dps.rm.repository.service.K8sServiceTypeRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import org.hibernate.reactive.stage.Stage.SessionFactory;

/**
 * This is the implementation of the #ServiceTypeService.
 *
 * @author matthi-g
 */
public class K8sServiceTypeServiceImpl extends DatabaseServiceProxy<K8sServiceType> implements K8sServiceTypeService {
    /**
     * Create an instance from the repository.
     *
     * @param repository  the repository
     */
    public K8sServiceTypeServiceImpl(K8sServiceTypeRepository repository, SessionFactory sessionFactory) {
        super(repository, K8sServiceType.class, sessionFactory);
    }
}