package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Function;
import org.hibernate.reactive.stage.Stage;

public class FunctionRepository extends Repository<Function> {

    public FunctionRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Function.class);
    }
}
