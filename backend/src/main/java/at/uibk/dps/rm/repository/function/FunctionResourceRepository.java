package at.uibk.dps.rm.repository.function;

import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class FunctionResourceRepository extends Repository<FunctionResource> {
    public FunctionResourceRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, FunctionResource.class);
    }

    public CompletionStage<FunctionResource> findByFunctionAndResource(long functionId, long resourceId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from FunctionResource fr " +
                        "where fr.function.functionId=:functionId and fr.resource.resourceId=:resourceId",
                    entityClass)
                .setParameter("functionId", functionId)
                .setParameter("resourceId", resourceId)
                .getSingleResultOrNull()
        );
    }

    public CompletionStage<List<FunctionResource>> findAllByReservationIdAndFetch(long reservationId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select distinct fr from ResourceReservation rr " +
                    "left join rr.functionResource fr " +
                    "left join fetch fr.function f " +
                    "left join fetch f.runtime " +
                    "left join fetch fr.resource r " +
                    "left join fetch r.metricValues mv " +
                    "left join fetch mv.metric " +
                    "left join fetch r.resourceType " +
                    "where rr.reservation.reservationId=:reservationId", entityClass)
                .setParameter("reservationId", reservationId)
                .getResultList());
    }

    public CompletionStage<Integer> deleteByFunctionAndResource(long functionId, long resourceId) {
        return this.sessionFactory.withTransaction(session ->
            session.createQuery("select fr.functionResourceId from FunctionResource fr " +
                        "where fr.function.functionId=:functionId and fr.resource.resourceId=:resourceId",
                    Long.class)
                .setParameter("functionId", functionId)
                .setParameter("resourceId", resourceId)
                .getSingleResult()
                .thenCompose(result -> session.createQuery("delete from FunctionResource fr " +
                        "where fr.functionResourceId=:functionResourceId")
                    .setParameter("functionResourceId", result)
                    .executeUpdate())
        );
    }
}
