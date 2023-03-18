package at.uibk.dps.rm.repository.reservation;

import at.uibk.dps.rm.entity.model.ResourceReservation;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class ResourceReservationRepository extends Repository<ResourceReservation> {
    public ResourceReservationRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceReservation.class);
    }

    public CompletionStage<List<ResourceReservation>> findAllByReservationId(long id) {
        return sessionFactory.withSession(session ->
                session.createQuery("select distinct rr from ResourceReservation rr " +
                                "left join fetch rr.functionResource fr " +
                                "left join fetch fr.resource " +
                                "left join fetch fr.function " +
                                "left join fetch rr.status " +
                                "where rr.reservation.reservationId=:id", entityClass)
                        .setParameter("id", id)
                        .getResultList()
        );
    }
}