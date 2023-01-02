package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.ResourceProvider;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class ResourceProviderRepository extends Repository<ResourceProvider> {
    public ResourceProviderRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, ResourceProvider.class);
    }

    public CompletionStage<ResourceProvider> findByProvider(String provider) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from ResourceProvider where provider=:provider", entityClass)
                .setParameter("provider", provider)
                .getSingleResultOrNull()
        );
    }
}
