package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Runtime;
import org.hibernate.reactive.stage.Stage;

public class RuntimeRepository extends Repository<Runtime> {
    public RuntimeRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Runtime.class);
    }
}
