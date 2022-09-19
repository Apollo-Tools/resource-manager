package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.ResourceReservation;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class ResourceReservationRepository extends Repository<ResourceReservation>{
    public ResourceReservationRepository(Stage.SessionFactory sessionFactory,
                                 Class<ResourceReservation> entityClass) {
        super(sessionFactory, entityClass);
    }

    public CompletionStage<List<ResourceReservation>> findAllByResourceId(long id) {
        return sessionFactory.withSession(session ->
                session.createQuery("select distinct rr from ResourceReservation rr " +
                                "left join fetch rr.resource r " +
                                "where rr.reservation.reservationId=:id", entityClass)
                        .setParameter("id", id)
                        .getResultList()
        );
    }
}