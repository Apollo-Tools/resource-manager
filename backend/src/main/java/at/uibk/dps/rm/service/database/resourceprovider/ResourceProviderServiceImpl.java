package at.uibk.dps.rm.service.database.resourceprovider;

import at.uibk.dps.rm.entity.model.ResourceProvider;
import at.uibk.dps.rm.repository.resourceprovider.ResourceProviderRepository;
import at.uibk.dps.rm.service.database.DatabaseServiceProxy;
import io.vertx.core.Future;

import java.util.Objects;

public class ResourceProviderServiceImpl extends DatabaseServiceProxy<ResourceProvider> implements ResourceProviderService {

    private final ResourceProviderRepository resourceProviderRepository;

    public ResourceProviderServiceImpl(ResourceProviderRepository repository) {
        super(repository, ResourceProvider.class);
        this.resourceProviderRepository = repository;
    }

    @Override
    public Future<Boolean> existsOneByProvider(String provider) {
        return Future
            .fromCompletionStage(resourceProviderRepository.findByProvider(provider))
            .map(Objects::nonNull);
    }
}
