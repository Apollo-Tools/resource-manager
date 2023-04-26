package at.uibk.dps.rm.repository.ensemble;

import at.uibk.dps.rm.entity.model.Ensemble;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

public class EnsembleRepository extends Repository<Ensemble> {

    public EnsembleRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Ensemble.class);
    }
}
