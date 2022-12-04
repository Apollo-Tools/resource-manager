package at.uibk.dps.rm.service.database.cloudprovider;

import at.uibk.dps.rm.entity.model.CloudProvider;
import at.uibk.dps.rm.repository.CloudProviderRepository;
import at.uibk.dps.rm.service.database.ServiceProxy;
import io.vertx.core.Future;

import java.util.Objects;

public class CloudProviderServiceImpl  extends ServiceProxy<CloudProvider> implements CloudProviderService {

    private final CloudProviderRepository cloudProviderRepository;

    public CloudProviderServiceImpl(CloudProviderRepository repository) {
        super(repository, CloudProvider.class);
        this.cloudProviderRepository = repository;
    }

    @Override
    public Future<Boolean> existsOneByProvider(String provider) {
        return Future
            .fromCompletionStage(cloudProviderRepository.findByProvider(provider))
            .map(Objects::nonNull);
    }
}
