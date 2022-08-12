package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.repository.Repository;
import at.uibk.dps.rm.entity.model.Property;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class PropertyRepository extends Repository<Property> {

    public PropertyRepository(Stage.SessionFactory sessionFactory,
                              Class<Property> entityClass) {
        super(sessionFactory, entityClass);
    }

    public CompletionStage<Property> findByProperty(String property) {
        return this.sessionFactory.withSession(session ->
                session.createQuery("from Property p where p.property=:property", entityClass)
                        .setParameter("property", property)
                        .getSingleResultOrNull()
        );
    }
}
