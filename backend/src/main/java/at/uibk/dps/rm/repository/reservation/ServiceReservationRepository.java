package at.uibk.dps.rm.repository.reservation;

import at.uibk.dps.rm.entity.model.ServiceReservation;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

public class ServiceReservationRepository extends Repository<ServiceReservation> {

    public ServiceReservationRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ServiceReservation.class);
    }
}
