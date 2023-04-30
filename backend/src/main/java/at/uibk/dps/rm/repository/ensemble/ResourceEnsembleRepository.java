package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.ResourceEnsemble;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

public class ResourceEnsembleRepository extends Repository<ResourceEnsemble> {
    public ResourceEnsembleRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceEnsemble.class);
    }
}
