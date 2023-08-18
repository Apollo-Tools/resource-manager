package at.uibk.dps.rm.repository.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.repository.Repository;
import org.hibernate.reactive.stage.Stage.Session;
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
     * Create an instance.
     */
    public FunctionRepository() {
        super(Function.class);
    }

    /**
     * Find a function by its id and fetch the runtime
     *
     * @param session the database session
     * @param id the id of the function
     * @return a CompletionStage that emits the function if it exists, else null
     */
    public CompletionStage<Function> findByIdAndFetch(Session session, long id) {
        return session.createQuery(
                "from Function f " +
                    "left join fetch f.runtime " +
                    "left join fetch f.functionType " +
                    "where f.functionId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull();
    }

    /**
     * Find a function by its name and runtime.
     *
     * @param session the database session
     * @param name the name of the function
     * @param runtimeId the id of the runtime
     * @return a CompletionStage that emits the function if it exists, else null
     */
    public CompletionStage<Function> findOneByNameAndRuntimeId(Session session, String name, long runtimeId) {
        return session.createQuery(
                "from Function f " +
                "left join fetch f.runtime r " +
                "left join fetch f.functionType " +
                "where f.name=:name and r.runtimeId =:runtimeId", entityClass)
            .setParameter("name", name)
            .setParameter("runtimeId", runtimeId)
            .getSingleResultOrNull();
    }

    /**
     * Find all functions and fetch the runtime.
     *
     * @param session the database session
     * @return a CompletionStage that emits the list of all functions
     */
    public CompletionStage<List<Function>> findAllAndFetch(Session session) {
        return session.createQuery("select distinct f from Function f " +
                "left join fetch f.runtime " +
                "left join fetch f.functionType", entityClass)
            .getResultList();
    }

    /**
     * Find all functions by their ids.
     *
     * @param session the database session
     * @param functionIds the list of function ids
     * @return a CompletionStage that emits the list of all functions
     */
    public CompletionStage<List<Function>> findAllByIds(Session session, Set<Long> functionIds) {
        if (functionIds.isEmpty()) {
            return CompletionStages.completedFuture(new ArrayList<>());
        }
        String functionIdsConcat = functionIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return session.createQuery("select distinct f from Function f " +
                "where f.functionId in (" + functionIdsConcat + ")", entityClass)
            .getResultList();
    }
}
