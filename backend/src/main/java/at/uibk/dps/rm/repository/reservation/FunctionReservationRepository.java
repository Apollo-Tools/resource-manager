package at.uibk.dps.rm.repository.reservation;

import at.uibk.dps.rm.entity.model.FunctionReservation;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class FunctionReservationRepository extends Repository<FunctionReservation> {

    public FunctionReservationRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, FunctionReservation.class);
    }

    /**
     * Find all function reservations by their reservation
     * @param reservationId the id of the reservation
     * @return a CompletionStage that emits a list of all function reservations
     */
    public CompletionStage<List<FunctionReservation>> findAllByReservationId(long reservationId) {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct fr from FunctionReservation fr " +
                    "left join fetch fr.function f " +
                    "left join fetch f.runtime " +
                    "left join fetch fr.resource r " +
                    "left join fetch r.metricValues mv " +
                    "left join fetch mv.metric " +
                    "left join fetch r.resourceType " +
                    "left join fetch r.region reg " +
                    "left join fetch reg.resourceProvider " +
                    "left join fetch fr.status " +
                    "where fr.reservation.reservationId=:reservationId", entityClass)
                .setParameter("reservationId", reservationId)
                .getResultList()
        );
    }
}
