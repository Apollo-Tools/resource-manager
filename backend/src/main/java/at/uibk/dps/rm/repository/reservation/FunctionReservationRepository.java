package at.uibk.dps.rm.repository.reservation;

import at.uibk.dps.rm.entity.model.FunctionReservation;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

public class FunctionReservationRepository extends Repository<FunctionReservation> {

    public FunctionReservationRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, FunctionReservation.class);
    }
}
