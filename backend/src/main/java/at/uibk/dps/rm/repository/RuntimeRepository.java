package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.Runtime;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class RuntimeRepository extends Repository<Runtime> {
    public RuntimeRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, Runtime.class);
    }

    public CompletionStage<Runtime> findByName(String name) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from Runtime where name=:name", entityClass)
                .setParameter("name", name)
                .getSingleResultOrNull()
        );
    }
}
