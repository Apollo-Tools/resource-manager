package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.FunctionResource;
import org.hibernate.reactive.stage.Stage;

public class FunctionResourceRepository extends Repository<FunctionResource> {
    public FunctionResourceRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, FunctionResource.class);
    }
}
