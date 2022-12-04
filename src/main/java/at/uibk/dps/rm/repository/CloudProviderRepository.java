package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.CloudProvider;
import org.hibernate.reactive.stage.Stage;

import java.util.concurrent.CompletionStage;

public class CloudProviderRepository extends Repository<CloudProvider> {
    public CloudProviderRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, CloudProvider.class);
    }

    public CompletionStage<CloudProvider> findByProvider(String provider) {
        return this.sessionFactory.withSession(session ->
            session.createQuery("from CloudProvider where provider=:provider", entityClass)
                .setParameter("provider", provider)
                .getSingleResultOrNull()
        );
    }
}
