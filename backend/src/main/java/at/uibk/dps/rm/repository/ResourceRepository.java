package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Resource;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class ResourceRepository extends Repository<Resource> {

    public ResourceRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Resource.class);
    }

    public CompletionStage<Resource> findByIdAndFetch(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
            "from Resource r left join fetch r.resourceType where r.resourceId =:id", entityClass)
                .setParameter("id", id)
                .getSingleResultOrNull()
            );
    }

    public CompletionStage<List<Resource>> findAllAndFetch(boolean excludeReservedResources) {
        final String excludeClause;

        if (excludeReservedResources) {
            excludeClause = "where r1.resourceId not in " +
                "(select distinct r2.resourceId from ResourceReservation rr " +
                "left join rr.reservation res " +
                "left join rr.resource r2 " +
                "where res.isActive=true)";
        } else {
            excludeClause = "";
        }


        return sessionFactory.withSession(session ->
            session.createQuery("select distinct r1 from Resource r1 " +
                            "left join fetch r1.metricValues mv " +
                            "left join fetch mv.metric " +
                            "left join fetch r1.resourceType " +
                            excludeClause, entityClass)
                .getResultList()
            );
    }

    public CompletionStage<List<Resource>> findByMultipleMetricsAndFetch(List<String> metrics) {
        return this.sessionFactory.withSession(session ->
                session.createQuery("select distinct r from Resource r " +
                        "left join fetch r.metricValues mv " +
                        "left join fetch r.resourceType " +
                        "left join fetch mv.metric m " +
                        "where m.metric in :metrics", Resource.class)
                        .setParameter("metrics", metrics)
                        .getResultList()
        );
    }

    public CompletionStage<List<Resource>> findAllByReservationIdAndFetch(long reservationId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select distinct r from ResourceReservation rr " +
                    "left join rr.reservation " +
                    "left join rr.resource r " +
                    "left join fetch r.resourceType " +
                    "left join fetch r.metricValues mv " +
                    "left join fetch mv.metric " +
                    "where rr.reservation.reservationId=:reservationId", Resource.class)
                .setParameter("reservationId", reservationId)
                .getResultList()
        );
    }

    public CompletionStage<List<Resource>> findByResourceType(long typeId) {
        return sessionFactory.withSession(session ->
            session.createQuery("from Resource r where r.resourceType.typeId=:typeId", entityClass)
                .setParameter("typeId", typeId)
                .getResultList());
    }

    public CompletionStage<Long> findByIdAndNotReserved(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
                "select distinct resource.resourceId from ResourceReservation rr " +
                        "right join rr.resource resource " +
                        "left join rr.reservation reservation " +
                        "where resource.resourceId=:id and (reservation.isActive=false or rr.resource is null)",
                        Long.class)
                .setParameter("id", id)
                .getSingleResultOrNull()
        );
    }

    public CompletionStage<List<Resource>> findAllByFunctionIdAndFetch(long functionId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select distinct r from FunctionResource fr " +
                    "left join fr.function f " +
                    "left join fr.resource r " +
                    "left join fetch r.resourceType " +
                    "left join fetch r.metricValues mv " +
                    "left join fetch mv.metric " +
                    "where f.functionId=:functionId", Resource.class)
                .setParameter("functionId", functionId)
                .getResultList()
        );
    }
}
