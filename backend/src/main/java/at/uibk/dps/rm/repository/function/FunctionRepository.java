package at.uibk.dps.rm.repository.function;

import at.uibk.dps.rm.entity.model.Function;
import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.service.database.util.SessionManager;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
     * Find a function by its id and fetch the runtime and the function type.
     *
     * @param sessionManager the database session manger
     * @param id the id of the function
     * @return a Maybe that emits the function if it exists, else null
     */
    public Maybe<Function> findByIdAndFetch(SessionManager sessionManager, long id) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery(
                "from Function f " +
                    "left join fetch f.runtime " +
                    "left join fetch f.functionType " +
                    "where f.functionId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }


    /**
     * Find a function by its id and accountId.
     *
     * @param sessionManager the database session manger
     * @param functionId the id of the function
     * @param accountId the id of the owner
     * @param includePublic whether to include public function
     * @return a Maybe that emits the entity if it exists, else null
     */
    public Maybe<Function> findByIdAndAccountId(SessionManager sessionManager, long functionId, long accountId,
            boolean includePublic) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Function f " +
                "left join fetch f.runtime " +
                "left join fetch f.functionType " +
                "left join fetch f.createdBy " +
                "where f.functionId=:functionId and " +
                "(f.createdBy.accountId=:accountId or (:includePublic=true and f.isPublic=true))", entityClass)
            .setParameter("functionId", functionId)
            .setParameter("accountId", accountId)
            .setParameter("includePublic", includePublic)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find a function by its name, function type and creator.
     *
     * @param sessionManager the database session manger
     * @param name the name of the function
     * @param typeId the id of the function
     * @param runtimeId the id of the runtime
     * @param accountId the id of the account
     * @return a Maybe that emits the function if it exists, else null
     */
    public Maybe<Function> findOneByNameTypeRuntimeAndCreator(SessionManager sessionManager, String name, long typeId,
            long runtimeId, long accountId) {
        return Maybe.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Function f " +
                    "where f.name=:name and f.functionType.artifactTypeId=:typeId and " +
                    "f.runtime.runtimeId=:runtimeId and f.createdBy.accountId=:accountId", entityClass)
            .setParameter("name", name)
            .setParameter("typeId", typeId)
            .setParameter("runtimeId", runtimeId)
            .setParameter("accountId", accountId)
            .getSingleResultOrNull()
        );
    }

    /**
     * Find all functions and fetch the runtime.
     *
     * @param sessionManager the database session manger
     * @return a Single that emits the list of all functions
     */
    public Single<List<Function>> findAllAndFetch(SessionManager sessionManager) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Function f " +
                "left join fetch f.runtime " +
                "left join fetch f.functionType", entityClass)
            .getResultList()
        );
    }

    /**
     * Find all functions by their ids.
     *
     * @param sessionManager the database session manger
     * @param functionIds the list of function ids
     * @return a Single that emits the list of all functions
     */
    public Single<List<Function>> findAllByIds(SessionManager sessionManager, Set<Long> functionIds) {
        if (functionIds.isEmpty()) {
            return Single.just(new ArrayList<>());
        }
        String functionIdsConcat = functionIds.stream().map(Object::toString).collect(Collectors.joining(","));
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("select distinct f from Function f " +
                "where f.functionId in (" + functionIdsConcat + ")", entityClass)
            .getResultList()
        );
    }


    /**
     * Find all accessible functions and fetch the function type and runtime.
     *
     * @param sessionManager the database session manger
     * @return a Single that emits a list of all function
     */
    public Single<List<Function>> findAllAccessibleAndFetch(SessionManager sessionManager, long accountId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Function f " +
                "left join fetch f.runtime " +
                "left join fetch f.functionType " +
                "left join fetch f.createdBy " +
                "where f.isPublic = true or f.createdBy.accountId=:accountId", entityClass)
            .setParameter("accountId", accountId)
            .getResultList()
        );
    }

    @Override
    public Single<List<Function>> findAllByAccountId(SessionManager sessionManager, long accountId) {
        return Single.fromCompletionStage(sessionManager.getSession()
            .createQuery("from Function f " +
                "left join fetch f.runtime " +
                "left join fetch f.functionType " +
                "where f.createdBy.accountId=:accountId", entityClass)
            .setParameter("accountId", accountId)
            .getResultList()
        );
    }
}
