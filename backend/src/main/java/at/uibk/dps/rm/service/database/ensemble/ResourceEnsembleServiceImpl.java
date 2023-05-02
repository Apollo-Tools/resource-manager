package at.uibk.dps.rm.service.database.ensemble;

import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.repository.ensemble.ResourceEnsembleRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;

public class ResourceEnsembleServiceImpl  extends DatabaseServiceProxy<ResourceEnsemble> implements
    ResourceEnsembleService {
    /**
     * Create an instance from the resourceEnsembleRepository.
     *
     * @param repository the resource ensemble repository
     */
    public ResourceEnsembleServiceImpl(ResourceEnsembleRepository repository) {
        super(repository, ResourceEnsemble.class);
    }
}
