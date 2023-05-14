package at.uibk.dps.rm.repository.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage;
import org.hibernate.reactive.util.impl.CompletionStages;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

/**
 * Implements database operations for the function entity.
 *
 * @author matthi-g
 */
public class FunctionRepository extends Repository<Function> {

    /**
     * Create an instance from the sessionFactory.
     *
     * @param sessionFactory the session factory
     */
    public FunctionRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Function.class);
    }

    /**
     * Find a function by its id and fetch the runtime
     *
     * @param id the id of the function
     * @return a CompletionStage that emits the function if it exists, else null
     */
    public CompletionStage<Function> findByIdAndFetch(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Function f left join fetch f.runtime where f.functionId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find a function by its name and runtime but exclude it if its id is equal to excludeId
     *
     * @param excludeId the id to be excluded from the result
     * @param name the name of the function
     * @param runtimeId the id of the runtime
     * @return a CompletionStage that emits the function if it exists, else null
     */
    public CompletionStage<Function> findOneByNameAndRuntimeId(long excludeId, String name, long runtimeId) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Function f " +
                    "left join fetch f.runtime r " +
                    "where f.functionId!=:excludeId and f.name=:name and r.runtimeId =:runtimeId", entityClass)
            .setParameter("excludeId", excludeId)
            .setParameter("name", name)
            .setParameter("runtimeId", runtimeId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find a function by its name and runtime.
     *
     * @param name the name of the function
     * @param runtimeId the id of the runtime
     * @return a CompletionStage that emits the function if it exists, else null
     */
    public CompletionStage<Function> findOneByNameAndRuntimeId(String name, long runtimeId) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Function f " +
                    "left join fetch f.runtime r " +
                    "where f.name=:name and r.runtimeId =:runtimeId", entityClass)
            .setParameter("name", name)
            .setParameter("runtimeId", runtimeId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find all functions and fetch the runtime.
     *
     * @return a CompletionStage that emits the list of all functions
     */
    public CompletionStage<List<Function>> findAllAndFetch() {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct f from Function f " +
                    "left join fetch f.runtime ",
                    entityClass)
                .getResultList()
        );
    }

    public CompletionStage<List<Function>> findAllByIds(Set<Long> functionIds) {
        if (functionIds.isEmpty()) {
            return CompletionStages.completedFuture(new ArrayList<>());
        }
        String functionIdsConcat = functionIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct f from Function f " +
                "where f.functionId in (" + functionIdsConcat + ")", entityClass)
                .getResultList()
        );
    }
}
