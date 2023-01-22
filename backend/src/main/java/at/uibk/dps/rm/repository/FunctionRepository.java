package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Function;
import org.hibernate.reactive.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class FunctionRepository extends Repository<Function> {

    public FunctionRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Function.class);
    }

    public CompletionStage<Function> findByIdAndFetch(long id) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Function f left join fetch f.runtime where f.functionId =:id", entityClass)
            .setParameter("id", id)
            .getSingleResultOrNull()
        );
    }

    public CompletionStage<List<Function>> findByNameAndRuntimeId(long excludeId, String name, long runtimeId) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Function f " +
                    "left join fetch f.runtime r " +
                    "where f.functionId!=:excludeId and f.name=:name and r.runtimeId =:runtimeId", entityClass)
            .setParameter("excludeId", excludeId)
            .setParameter("name", name)
            .setParameter("runtimeId", runtimeId)
            .getResultList()
        );
    }

    public CompletionStage<List<Function>> findByNameAndRuntimeId(String name, long runtimeId) {
        return sessionFactory.withSession(session -> session.createQuery(
                "from Function f " +
                    "left join fetch f.runtime r " +
                    "where f.name=:name and r.runtimeId =:runtimeId", entityClass)
            .setParameter("name", name)
            .setParameter("runtimeId", runtimeId)
            .getResultList()
        );
    }

    public CompletionStage<List<Function>> findAllAndFetch() {
        return sessionFactory.withSession(session ->
            session.createQuery("select distinct f from Function f " +
                    "left join fetch f.runtime ",
                    entityClass)
                .getResultList()
        );
    }
}
