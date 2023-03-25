package at.uibk.dps.rm.repository.reservation;

import at.uibk.dps.rm.entity.deployment.ReservationStatusValue;
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

    public CompletionStage<Integer> updateTriggerUrl(long functionResourceId, long reservationId, String triggerUrl) {
        return sessionFactory.withSession(session ->
            session.createQuery("update ResourceReservation rr " +
                "set triggerUrl=:triggerUrl, isDeployed=true " +
                "where rr.functionResource.functionResourceId=:functionResourceId and " +
                "rr.reservation.reservationId=:reservationId")
                .setParameter("triggerUrl", triggerUrl)
                .setParameter("functionResourceId", functionResourceId)
                .setParameter("reservationId", reservationId)
                .executeUpdate()
        );
    }

    public CompletionStage<Integer> updateReservationStatusByReservationId(long reservationId,
                                                                        ReservationStatusValue statusValue) {
        return sessionFactory.withSession(session ->
            session.createQuery("update ResourceReservation rr " +
                "set status.statusId=" +
                "(select rrs.statusId from ResourceReservationStatus rrs where rrs.statusValue=:statusValue)" +
                "where rr.reservation.reservationId=:reservationId")
                .setParameter("reservationId", reservationId)
                .setParameter("statusValue", statusValue.toString())
                .executeUpdate()
        );
    }
}
