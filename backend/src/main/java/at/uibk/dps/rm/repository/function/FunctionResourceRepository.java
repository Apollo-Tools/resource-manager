package at.uibk.dps.rm.repository.function;

import at.uibk.dps.rm.entity.model.FunctionResource;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements database operations for the function resource entity.
 *
 * @author matthi-g
 */
public class FunctionResourceRepository extends Repository<FunctionResource> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public FunctionResourceRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, FunctionResource.class);
    }

    /**
     * Find a function resource by its function and resource
     *
     * @param functionId the id of the function
     * @param resourceId the id of the resource
     * @return a CompletionStage that emits the function resource if it exist, else null
     */
    public CompletionStage<FunctionResource> findByFunctionAndResource(long functionId, long resourceId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from FunctionResource fr " +
                        "left join fetch fr.resource r " +
                        "left join fetch r.region reg " +
                        "left join fetch reg.resourceProvider " +
                        "where fr.function.functionId=:functionId and fr.resource.resourceId=:resourceId",
                    entityClass)
                .setParameter("functionId", functionId)
                .setParameter("resourceId", resourceId)
                .getSingleResultOrNull()
        );
    }

    /**
     * Find all function resources by their reservation
     *
     * @param reservationId the id of the reservation
     *
     * @return a CompletionStage that emits a list of al function resources if it exists, else null
     */
    public CompletionStage<List<FunctionResource>> findAllByReservationIdAndFetch(long reservationId) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("select distinct fr from ResourceReservation rr " +
                    "left join rr.functionResource fr " +
                    "left join fetch fr.function f " +
                    "left join fetch f.runtime " +
                    "left join fetch fr.resource r " +
                    "left join fetch r.metricValues mv " +
                    "left join fetch mv.metric " +
                    "left join fetch r.platform p " +
                    "left join fetch p.resourceType " +
                    "left join fetch r.region reg " +
                    "left join fetch reg.resourceProvider " +
                    "where rr.reservation.reservationId=:reservationId", entityClass)
                .setParameter("reservationId", reservationId)
                .getResultList());
    }

    /**
     * Delete a function resource by its function and resource.
     *
     * @param functionId the id of the function
     * @param resourceId the id of the resource
     * @return a CompletionStage that emits the row count
     */
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
