package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.ResourceReservation;
import org.hibernate.reactive.stage.Stage;

public class ResourceReservationRepository extends Repository<ResourceReservation>{
    public ResourceReservationRepository(Stage.SessionFactory sessionFactory,
                                 Class<ResourceReservation> entityClass) {
        super(sessionFactory, entityClass);
    }
}
