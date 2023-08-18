package at.uibk.dps.rm.repository.service;

import at.uibk.dps.rm.entity.model.K8sServiceType;
import at.uibk.dps.rm.repository.Repository;

/**
 * Implements database operations for the service_type entity.
 *
 * @author matthi-g
 */
public class K8sServiceTypeRepository extends Repository<K8sServiceType> {
    /**
     * Create an instance.
     */
    public K8sServiceTypeRepository() {
        super(K8sServiceType.class);
    }
}
