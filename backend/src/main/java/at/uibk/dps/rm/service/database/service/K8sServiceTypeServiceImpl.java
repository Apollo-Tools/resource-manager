package at.uibk.dps.rm.service.database.service;

import at.uibk.dps.rm.entity.model.K8sServiceType;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import at.uibk.dps.rm.repository.service.K8sServiceTypeRepository;
import at.uibk.dps.rm.service.database.util.SessionManagerProvider;

/**
 * This is the implementation of the {@link K8sServiceTypeService}.
 *
 * @author matthi-g
 */
public class K8sServiceTypeServiceImpl extends DatabaseServiceProxy<K8sServiceType> implements K8sServiceTypeService {
    /**
     * Create an instance from the repository.
     *
     * @param repository  the repository
     */
    public K8sServiceTypeServiceImpl(K8sServiceTypeRepository repository, SessionManagerProvider smProvider) {
        super(repository, K8sServiceType.class, smProvider);
    }
}
