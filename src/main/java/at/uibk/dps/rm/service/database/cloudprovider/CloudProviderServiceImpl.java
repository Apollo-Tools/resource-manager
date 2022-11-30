package at.uibk.dps.rm.service.database.cloudprovider;

import at.uibk.dps.rm.entity.model.CloudProvider;
import at.uibk.dps.rm.repository.CloudProviderRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;

public class CloudProviderServiceImpl  extends ServiceProxy<CloudProvider> implements CloudProviderService {
    public CloudProviderServiceImpl(CloudProviderRepository repository) {
        super(repository, CloudProvider.class);
    }
}
