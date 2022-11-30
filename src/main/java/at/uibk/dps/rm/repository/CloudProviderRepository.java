package at.uibk.dps.rm.repository;

import at.uibk.dps.rm.entity.model.CloudProvider;
import org.hibernate.reactive.stage.Stage;

public class CloudProviderRepository   extends Repository<CloudProvider> {
    public CloudProviderRepository(Stage.SessionFactory sessionFactory) {
        super(sessionFactory, CloudProvider.class);
    }
}
